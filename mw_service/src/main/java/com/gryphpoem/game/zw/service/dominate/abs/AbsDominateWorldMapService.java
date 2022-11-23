package com.gryphpoem.game.zw.service.dominate.abs;

import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.DominateSideCity;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.DominateSideGovernor;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.WorldMapPlay;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.abs.TimeLimitDominateMap;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.impl.SiLiDominateWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.impl.StateDominateWorldMap;
import com.gryphpoem.game.zw.manager.MedalDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb8;
import com.gryphpoem.game.zw.pb.WorldPb;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.WorldService;
import com.gryphpoem.game.zw.service.dominate.IDominateWorldMapService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-22 21:33
 */
public abstract class AbsDominateWorldMapService implements IDominateWorldMapService {

    @Autowired
    protected PlayerDataManager playerDataManager;
    @Autowired
    protected WorldService worldService;
    @Autowired
    protected RewardDataManager rewardDataManager;
    @Autowired
    protected MedalDataManager medalDataManager;
    @Autowired
    protected WorldDataManager worldDataManager;

    @Override
    public GamePb8.GetDominateWorldMapInfoRs getDominateWorldMapInfo(long roleId, GamePb8.GetDominateWorldMapInfoRq req) {
        playerDataManager.checkPlayerIsExist(roleId);
        WorldMapPlay worldMapPlay = getWorldMapPlay(req.getWorldFunction());
        if (CheckNull.isNull(worldMapPlay)) {
            throw new MwException(GameError.PARAM_ERROR, String.format("不能存的地图玩法, req:%d", req.getWorldFunction()));
        }

        GamePb8.GetDominateWorldMapInfoRs.Builder builder = GamePb8.GetDominateWorldMapInfoRs.newBuilder();
        builder.setBaseFunction(worldMapPlay.createPb(false));
        return builder.build();
    }

    @Override
    public GamePb8.AttackDominateCityRs attackDominateCity(long roleId, GamePb8.AttackDominateCityRq req) {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        WorldMapPlay worldMapPlay = getWorldMapPlay(req.getWorldFunction());
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(req.getCityId());
        if (CheckNull.isNull(worldMapPlay) || CheckNull.isNull(staticCity) || !worldMapPlay.isOpen()) {
            throw new MwException(GameError.PARAM_ERROR, String.format("不存在的地图玩法, req:%d", req.getWorldFunction()));
        }
        if (worldMapPlay.state() != WorldPb.WorldFunctionStateDefine.IN_PROGRESS_VALUE) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN, String.format("活动未开始, req:%d", req.getWorldFunction()));
        }
        TimeLimitDominateMap timeLimitDominateMap = (TimeLimitDominateMap) worldMapPlay;
        List<DominateSideCity> sideCityList = timeLimitDominateMap.getCurOpenCityList().get(timeLimitDominateMap.getCurTimes());
        DominateSideCity dominateSideCity;
        if (CheckNull.isEmpty(sideCityList) || Objects.isNull(dominateSideCity = sideCityList.stream().filter(city ->
                city.getCityId() == req.getCityId()).findFirst().orElse(null))) {
            throw new MwException(GameError.NO_EXIST_CITY, String.format("没有此城池, req:%d, cityId:%d", req.getWorldFunction(), req.getCityId()));
        }

        int now = TimeHelper.getCurrentSecond();
        if (WorldConstant.ATTACK_STATE_NEED_LV > player.lord.getLevel()) {
            throw new MwException(GameError.ATTACK_STATE_NEED_LV.getCode(), "指挥官先磨砺至45级，再发动阵营战吧, roleId:", roleId);
        }
        if (dominateSideCity.isOver()) {
            throw new MwException(GameError.CITY_HAS_BEEN_VANQUISHED, String.format("城池已被攻克, req:%d, cityId:%d", req.getWorldFunction(), req.getCityId()));
        }
        checkArmy(player, dominateSideCity);

        List<Integer> heroIdList = new ArrayList<>();
        heroIdList.addAll(req.getHeroIdList());
        heroIdList = heroIdList.stream().distinct().collect(Collectors.toList());
        // 检查出征将领信息
        worldService.checkFormHeroSupport(player, heroIdList, staticCity.getCityPos());

        Hero hero;
        int armCount = 0;
        List<CommonPb.TwoInt> form = new ArrayList<>();
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            form.add(PbHelper.createTwoIntPb(heroId, hero.getCount()));
            armCount += hero.getCount();
        }

        int marchTime = worldService.marchTime(player, staticCity.getCityPos());
        // 雄踞一方行军时间减半
        marchTime = (int) (marchTime * Constant.DOMINATE_MARCH_SPEEDUP / NumberUtil.TEN_THOUSAND_DOUBLE);
        // 计算时间是否赶得上
        int endTime = (int) (timeLimitDominateMap.getCurEndTime().getTime() / 1000l);
        if (now + marchTime > endTime) {
            throw new MwException(GameError.BATTLE_CD_TIME.getCode(), "加入城战,赶不上时间, roleId:", roleId, ", pos:",
                    staticCity.getCityPos() + ",行军时间=" + (now + marchTime) + ",城战倒计时=" + endTime);
        }

        // 校验粮食, 并扣除粮食同步通知
        int needFood = worldService.checkMarchFood(player, marchTime, armCount);
        // 行军时间减少, 粮食消耗减少
        rewardDataManager.subPlayerResHasChecked(player, AwardType.RESOURCE, AwardType.Resource.FOOD,
                needFood, AwardFrom.ATK_POS);
        ChangeInfo change = ChangeInfo.newIns();
        change.addChangeType(AwardType.RESOURCE, AwardType.Resource.FOOD);
        // 向客户端同步玩家资源数据
        rewardDataManager.syncRoleResChanged(player, change);

        // 创建部队
        Army army = new Army(player.maxKey(), getArmyType(worldMapPlay), staticCity.getCityPos(), ArmyConstant.ARMY_STATE_MARCH, form, marchTime - 1,
                now + marchTime - 1, player.getDressUp());
        army.setTargetId(staticCity.getCityId());
        army.setLordId(roleId);
        army.setOriginPos(player.lord.getPos());
        // 名城buffer记录
        army.setOriginCity(worldDataManager.checkCityBuffer(player.lord.getPos()));
        army.setHeroMedals(heroIdList.stream()
                .map(heroId -> medalDataManager.getHeroMedalByHeroIdAndIndex(player, heroId, MedalConst.HERO_MEDAL_INDEX_0))
                .filter(Objects::nonNull)
                .map(PbHelper::createMedalPb)
                .collect(Collectors.toList()));
        player.armys.put(army.getKeyId(), army);
        // 添加行军路线
        March march = new March(player, army);
        worldDataManager.addMarch(march);

        // 返回协议
        GamePb8.AttackDominateCityRs.Builder builder = GamePb8.AttackDominateCityRs.newBuilder();

        builder.setArmy(PbHelper.createArmyPb(army, false));
        // 区域变化推送
        List<Integer> posList = MapHelper.getAreaStartPos(MapHelper.getLineAcorss(staticCity.getCityPos(), player.lord.getPos()));
        posList.add(staticCity.getCityPos());
        posList.add(player.lord.getPos());
        EventBus.getDefault().post(
                new Events.AreaChangeNoticeEvent(posList, roleId, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        return builder.build();
    }

    @Override
    public GamePb8.GetDominateDetailRs getDominateDetail(long roleId, GamePb8.GetDominateDetailRq req) {
        playerDataManager.checkPlayerIsExist(roleId);
        WorldMapPlay worldMapPlay = getWorldMapPlay(req.getWorldFunction());
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(req.getCityId());
        if (CheckNull.isNull(worldMapPlay) || CheckNull.isNull(staticCity)) {
            throw new MwException(GameError.PARAM_ERROR, String.format("不存在的地图玩法, req:%d", req.getWorldFunction()));
        }
        City city = worldDataManager.getCityById(req.getCityId());
        if (CheckNull.isNull(city)) {
            throw new MwException(GameError.NO_EXIST_CITY, String.format("没有此城池, req:%d, cityId:%d", req.getWorldFunction(), req.getCityId()));
        }
        int state = worldMapPlay.state();
        GamePb8.GetDominateDetailRs.Builder builder = GamePb8.GetDominateDetailRs.newBuilder().setState(state);
        if ((city instanceof DominateSideCity) == false) {
            // 一次活动还没开始
            return builder.build();
        }

        DominateSideCity sideCity = (DominateSideCity) city;
        switch (state) {
            case WorldPb.WorldFunctionStateDefine.IN_PROGRESS_VALUE:
                LinkedList<Turple<Long, Integer>> defendList =  sideCity.getDefendList();
                if (CheckNull.nonEmpty(defendList)) {
                    builder.addAllProbArmy(defendList.stream().map(data -> getBaseArmyPb(data, sideCity)).collect(Collectors.toList()));
                }
                break;
            default:
                LinkedList<DominateSideGovernor> governors = sideCity.getGovernorList();
                if (CheckNull.nonEmpty(governors)) {
                    DominateSideGovernor governor = governors.peekFirst();
                    if (Objects.nonNull(governor)) {
                        Player player = playerDataManager.getPlayer(governor.getRoleId());
                        if (Objects.nonNull(player)) {
                            builder.setCurGovernorCamp(player.lord.getCamp());
                            builder.setCurGovernorNick(player.lord.getNick());
                            builder.setPortrait(player.lord.getPortrait());
                        }
                    }
                }
        }

        return builder.build();
    }

    @Override
    public GamePb8.GetDominateGovernorListRs getDominateGovernorList(long roleId, GamePb8.GetDominateGovernorListRq req) {
        playerDataManager.checkPlayerIsExist(roleId);
        WorldMapPlay worldMapPlay = getWorldMapPlay(req.getWorldFunction());
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(req.getCityId());
        if (CheckNull.isNull(worldMapPlay) || CheckNull.isNull(staticCity)) {
            throw new MwException(GameError.PARAM_ERROR, String.format("不存在的地图玩法, req:%d", req.getWorldFunction()));
        }
        City city = worldDataManager.getCityById(req.getCityId());
        if (CheckNull.isNull(city)) {
            throw new MwException(GameError.NO_EXIST_CITY, String.format("没有此城池, req:%d, cityId:%d", req.getWorldFunction(), req.getCityId()));
        }
        int state = worldMapPlay.state();
        GamePb8.GetDominateGovernorListRs.Builder builder = GamePb8.GetDominateGovernorListRs.newBuilder().setState(state);
        if ((city instanceof DominateSideCity) == false) {
            // 一次活动还没开始
            return builder.build();
        }

        DominateSideCity sideCity = (DominateSideCity) city;
        LinkedList<DominateSideGovernor> governors = sideCity.getGovernorList();
        if (CheckNull.nonEmpty(governors)) {
            governors.forEach(governor -> {
                Player player = playerDataManager.getPlayer(governor.getRoleId());
                if (CheckNull.isNull(player)) return;
                builder.addGovernor(PbHelper.createDominateSideGovernorPb(player, governor));
            });
        }
        return builder.build();
    }

    @Override
    public void syncDominateWorldMapInfo() {

    }

    protected int getArmyType(WorldMapPlay worldMapPlay) {
        if (worldMapPlay instanceof StateDominateWorldMap) {
            return ArmyConstant.ARMY_TYPE_STATE_DOMINATE_ATTACK;
        }
        if (worldMapPlay instanceof SiLiDominateWorldMap) {

        }

        return -1;
    }

    protected WorldMapPlay getWorldMapPlay(int worldFunction) {
        WorldMapPlay worldMapPlay = null;
        switch (worldFunction) {
            case WorldPb.WorldFunctionDefine.STATES_AND_COUNTIES_DOMINATE_VALUE:
                worldMapPlay = StateDominateWorldMap.getInstance();
                break;
            case WorldPb.WorldFunctionDefine.SI_LI_DOMINATE_SIDE_VALUE:
                worldMapPlay = SiLiDominateWorldMap.getInstance();
                break;
        }

        return worldMapPlay;
    }

    /**
     * 校验防守部队是否已满
     *
     * @param player
     * @param sideCity
     * @throws MwException
     */
    protected void checkArmy(Player player, DominateSideCity sideCity) throws MwException {
        if (sideCity.getHoldCamp() == player.lord.getCamp() && sideCity.getDefendList().size() >= Constant.MAXIMUM_NUMBER_OF_DOMINATE_DEFENSE_QUEUE) {
            throw new MwException(GameError.TROOPS_OCCUPYING_NUM_SIDE_DEFENSE_QUEUE_HAS_REACHED_UPPER_LIMIT.getCode(),
                    GameError.err(player.roleId, "雄踞一方, 雄踞一方防守队列军团数量达到上限"));
        }
    }

    /**
     * 创建armyPb
     *
     * @param data
     * @param sideCity
     * @return
     */
    protected WorldPb.BaseWorldMapArmy getBaseArmyPb(Turple<Long, Integer> data, DominateSideCity sideCity) {
        if (CheckNull.isNull(data)) return null;
        Player player = playerDataManager.getPlayer(data.getA());
        if (CheckNull.isNull(player)) return null;
        Army army = player.armys.get(data.getB());
        if (CheckNull.isNull(army)) return null;
        WorldPb.BaseWorldMapArmy.Builder builder = WorldPb.BaseWorldMapArmy.newBuilder().setLordLv(player.lord.getLevel()).
                setArmyLead(army.getArmCount()).setLordName(player.lord.getNick());
        List<CommonPb.TwoInt> fatigueDeBuffList;
        if ((fatigueDeBuffList = checkFatigueDeBuff(sideCity.holdTime(data.getA(), data.getB()))) != null)
            builder.addAllFatigueDeBuff(fatigueDeBuffList);
        return builder.build();
    }

    /**
     * 获取疲劳buff信息
     *
     * @param holdTime
     * @return
     */
    protected List<CommonPb.TwoInt> checkFatigueDeBuff(long holdTime) {
        if (holdTime == 0l) return null;
        if (CheckNull.isEmpty(Constant.DOMINATE_FATIGUE_DE_BUFF_PARAMETER))
            return null;
        long nowMills = System.currentTimeMillis();
        long intervalTime = (nowMills - holdTime) / 1000l;
        return Constant.DOMINATE_FATIGUE_DE_BUFF_PARAMETER.stream().map(config_ -> {
            int attrId = config_.get(0), ratio = 0;
            if (intervalTime >= config_.get(1)) {
                ratio += config_.get(3);
            } else
                return null;
            ratio += (intervalTime - config_.get(1)) / config_.get(2) * config_.get(3);
            ratio = Math.min(ratio, config_.get(4));
            if (ratio <= 0) return null;
            return PbHelper.createTwoIntPb(attrId, ratio);
        }).filter(t -> Objects.nonNull(t)).collect(Collectors.toList());
    }
}
