package com.gryphpoem.game.zw.service.dominate.abs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.DominateSideCity;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.DominateSideGovernor;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.WorldMapPlay;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.abs.TimeLimitDominateMap;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.impl.SiLiDominateWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.impl.StateDominateWorldMap;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb8;
import com.gryphpoem.game.zw.pb.WorldPb;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.FightService;
import com.gryphpoem.game.zw.service.PlayerService;
import com.gryphpoem.game.zw.service.WarService;
import com.gryphpoem.game.zw.service.WorldService;
import com.gryphpoem.game.zw.service.dominate.DominateWorldMapService;
import com.gryphpoem.game.zw.service.dominate.IDominateWorldMapService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
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
    @Autowired
    protected ChatDataManager chatDataManager;
    @Autowired
    protected FightService fightService;
    @Autowired
    protected WarService warService;
    @Autowired
    protected MailDataManager mailDataManager;
    @Autowired
    protected PlayerService playerService;

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
        GamePb8.SyncDominateMapInfoRs.Builder builder = GamePb8.SyncDominateMapInfoRs.newBuilder();
        builder.setBaseFunction(getWorldMapPlay(getWorldMapFunction()).createPb(false));
        BasePb.Base base = PbHelper.createSynBase(GamePb8.SyncDominateMapInfoRs.EXT_FIELD_NUMBER, GamePb8.SyncDominateMapInfoRs.ext, builder.build()).build();
        Optional.ofNullable(playerDataManager.getAllOnlinePlayer().values()).ifPresent(list -> {
            list.forEach(player -> {
                playerService.syncMsgToPlayer(base, player);
            });
        });
    }

    /**
     * 不同活动玩法获取不同部队类型
     *
     * @param worldMapPlay
     * @return
     */
    protected int getArmyType(WorldMapPlay worldMapPlay) {
        if (worldMapPlay instanceof StateDominateWorldMap) {
            return ArmyConstant.ARMY_TYPE_STATE_DOMINATE_ATTACK;
        }
        if (worldMapPlay instanceof SiLiDominateWorldMap) {
            return ArmyConstant.ARMY_TYPE_SI_LI_DOMINATE_ATTACK;
        }

        return -1;
    }

    /**
     * 进入防守阵列, 修改部队状态
     *
     * @param worldMapPlay
     * @return
     */
    protected int getDefenceArmyType(WorldMapPlay worldMapPlay) {
        if (worldMapPlay instanceof StateDominateWorldMap) {
            return ArmyConstant.ARMY_STATE_STATE_DOMINATE_HOLDER;
        }
        if (worldMapPlay instanceof SiLiDominateWorldMap) {
            return ArmyConstant.ARMY_STATE_SI_LI_DOMINATE_HOLDER;
        }

        return -1;
    }

    /**
     * 不同地图玩法获取不同地图实例
     *
     * @param worldFunction
     * @return
     */
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


    @Override
    public void marchEnd(Player player, Army army, int nowSec) {
        try {
            long roleId = player.roleId;
            int tarPos = army.getTarget();
            //活动未开启
            StateDominateWorldMap worldMap = StateDominateWorldMap.getInstance();
            if (worldMap.state() != WorldPb.WorldFunctionStateDefine.IN_PROGRESS_VALUE) {
                // 州郡雄踞一方活动未开启
                LogUtil.error(String.format("dominate not open!!! roleId :%d, armyKeyId :%d, target pos :%d, march end time :%d",
                        roleId, army.getKeyId(), tarPos, army.getEndTime()));
                retreatArmy(player, army, null, null, nowSec, false);
                return;
            }

            // 城池不存在
            WorldMapPlay worldMapPlay = getWorldMapPlay(getWorldMapFunction());
            TimeLimitDominateMap timeLimitDominateMap = (TimeLimitDominateMap) worldMapPlay;
            List<DominateSideCity> sideCityList = timeLimitDominateMap.getCurOpenCityList().get(timeLimitDominateMap.getCurTimes());
            DominateSideCity sideCity;
            if (CheckNull.isEmpty(sideCityList) || Objects.isNull(sideCity = sideCityList.stream().filter(city ->
                    city.getCityId() == army.getTargetId()).findFirst().orElse(null))) {
                LogUtil.error(String.format("dominate city not found, army: ", armyHeroString(army)));
                return;
            }
            //遗迹已经被我方占领. 加入驻防队列
            if (sideCity.getHoldCamp() == player.getCamp()) {
                joinProbing(sideCity, player, army, TimeHelper.getCurrentSecond());
                return;
            }
            //没有防守部队 直接占领
            LinkedList<Turple<Long, Integer>> defendList = sideCity.getDefendList();
            if (CheckNull.isEmpty(defendList)) {
                handAttackDominateSuccess(sideCity, player, army, nowSec);
                return;
            }
            //开始战斗
            doFight(sideCity, player, army, nowSec);
        } catch (Exception e) {
            LogUtil.error(String.format("roleId :%d, armyKeyId :%d, heroList :%s", player.roleId, army.getKeyId(), armyHeroString(army)), e);
            //行军失败!!! 返回部队
            worldService.retreatArmy(player, army, nowSec);
        }
    }

    /**
     * 战斗
     *
     * @param sideCity
     * @param player
     * @param army
     * @param nowSec
     */
    public void doFight(DominateSideCity sideCity, Player player, Army army, int nowSec) {
        LinkedList<Turple<Long, Integer>> defendList = sideCity.getDefendList();
        int fightCount = 0;
        do {
            Turple<Long, Integer> tpl = defendList.peekFirst();
            if (Objects.isNull(tpl)) {
                break;//防守队列被打光了
            }
            //进攻部队被打光了
            if (army.getHeroLeadCount() <= 0) {
                LogUtil.common(String.format("cityId :%d, roleId :%d, armyId :%d, army hero all dead...", sideCity.getCityId(), player.roleId, army.getKeyId()));
                break;
            }

            long defendRoleId = tpl.getA();
            int defendArmyKeyId = tpl.getB();
            Player defendPlayer = playerDataManager.getPlayer(defendRoleId);
            Army defendArmy = Objects.nonNull(defendPlayer) ? defendPlayer.armys.get(defendArmyKeyId) : null;
            //防守玩家的部队不存在
            if (Objects.isNull(defendArmy) || defendArmy.getHeroLeadCount() <= 0 || defendArmy.getType() != getDefenceArmyType(getWorldMapPlay(getWorldMapFunction()))) {
                LogUtil.error(String.format("cityId :%d, defend roleId :%d, defend army keyId :%d, not found!!!", sideCity.getCityId(), defendRoleId, defendArmyKeyId));
                defendList.removeFirst();
                continue;
            }
            FightLogic fightLogic = fightLogic(sideCity, player, army, defendPlayer, defendArmy, nowSec, tpl);
            if (Objects.isNull(fightLogic)) {
                LogUtil.error(String.format("dominate fight occur error,cityId :%d, roleId :%d, army keyId :%d, def roleId :%d, def army keyId :%d, fight is error...",
                        sideCity.getCityId(), player.roleId, army.getKeyId(), defendRoleId, defendArmyKeyId));
                break;
            }
        } while (++fightCount < Constant.DOMINATE_ARMY_FIGHT_MAX);

        //记录一下战斗超过10场的玩家与出战部队信息
        if (fightCount > 10) {
            LogUtil.common(String.format("cityId :%d, roleId :%d, army keyId :%d, hero list :%s, fight count so much !!!",
                    sideCity.getCityId(), player.roleId, army.getKeyId(), armyHeroString(army)));
        }

        //攻打遗迹战斗结束[进攻胜利, 防守胜利, 战斗异常]
        if (army.getHeroLeadCount() > 0) {
            if (defendList.isEmpty()) {
                handAttackDominateSuccess(sideCity, player, army, nowSec);
            } else {//达到战斗次数上限还没打下来 就返回
                //never got here except fight error!!!
                LogUtil.error(String.format("cityId :%d, roleId :%d army keyId :%d, survival hero list :%s. defend remain size :%d",
                        sideCity.getCityId(), player.roleId, army.getKeyId(), armyHeroString(army), defendList.size()));
                retreatArmy(player, army, null, sideCity, nowSec, false);
            }
        }
    }

    /**
     * 战斗逻辑
     *
     * @param rlc
     * @param attackPlayer
     * @param atkArmy
     * @param defendPlayer
     * @param defArmy
     * @param nowSec
     * @param o2
     * @return
     */
    private FightLogic fightLogic(DominateSideCity rlc, Player attackPlayer, Army atkArmy, Player defendPlayer, Army defArmy, int nowSec, Turple<Long, Integer> o2) {
        long fightId = 0;
        try {
            Fighter attacker = fightService.createFighterWithFatigueDeBuff(attackPlayer, atkArmy.getKeyId(), atkArmy.getHero(), null, 0);
            Fighter defender = fightService.createFighterWithFatigueDeBuff(defendPlayer, defArmy.getKeyId(), defArmy.getHero(),
                    Constant.DOMINATE_FATIGUE_DE_BUFF_PARAMETER, rlc.holdTime(defendPlayer.roleId, defArmy.getKeyId()));
            FightLogic fightLogic = new FightLogic(attacker, defender, true, WorldConstant.BATTLE_TYPE_HIS_REMAIN);
            fightLogic.packForm();
            fightLogic.fight();
            fightId = fightLogic.fightId;
            LogUtil.debug(String.format("his cityId :%d, fightId :%d, result :%s, attack roleId :%d, atk army keyId :%d, defend roleId :%d, army keyId :%d",
                    rlc.getCityId(), fightId, fightLogic.getWinState(), attackPlayer.roleId, atkArmy.getKeyId(), defendPlayer.roleId, defArmy.getKeyId()));

            Map<Long, ChangeInfo> changeMap = new HashMap<>(); // 记录需要推送的值
            Map<Long, Integer> recoverMap = new HashMap<>();
            // 损兵与伤病恢复
            if (attacker.lost > 0) {
                subAndRetreatDeadHeroInArmy(attackPlayer, attacker, atkArmy, changeMap, recoverMap, rlc, nowSec);
                //进攻方兵力=0 则返回部队
                if (atkArmy.getHeroLeadCount() <= 0) {
                    retreatArmy(attackPlayer, atkArmy, changeMap, rlc, nowSec, false);
                }
            }
            if (defender.lost > 0) {
                subAndRetreatDeadHeroInArmy(defendPlayer, defender, defArmy, changeMap, recoverMap, rlc, nowSec);
                if (defArmy.getHeroLeadCount() <= 0) {
                    rlc.getDefendList().remove(o2);
                    retreatArmy(defendPlayer, defArmy, changeMap, rlc, nowSec, true);
                }
            }

            //创建战报
            CommonPb.Report.Builder report = createFightReport(fightLogic, attackPlayer, defendPlayer, recoverMap, nowSec, changeMap);
            // 通知客户端玩家兵力/战功等资源变化
            warService.sendRoleResChange(changeMap);
            boolean attackSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;
            //发送战斗邮件
            if (attackSuccess) {
                attackFightSuccess(fightLogic, rlc, report, attackPlayer, atkArmy, defendPlayer, nowSec);
            } else {
                attackFightFailure(fightLogic, rlc, report, attackPlayer, atkArmy, defendPlayer, nowSec);
            }
            // 地图刷新
            List<Integer> posList = MapHelper.getAreaStartPos(MapHelper.getLineAcorss(atkArmy.getTarget(), attackPlayer.lord.getPos()));
            posList.add(atkArmy.getTarget());
            posList.add(attackPlayer.lord.getPos());
            // 新增后台埋点日志
            EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
            LogLordHelper.otherLog("battle", attackPlayer.account.getServerId(), attackPlayer.roleId, "atk", 0, WorldConstant.BATTLE_TYPE_HIS_REMAIN,
                    fightLogic.getWinState(), atkArmy.getTarget(), attackPlayer.lord.getLordId(), defendPlayer.lord.getLordId(), attackPlayer.lord.getCamp());
            LogLordHelper.otherLog("battle", defendPlayer.account.getServerId(), defendPlayer.roleId, "def", 0, WorldConstant.BATTLE_TYPE_HIS_REMAIN,
                    fightLogic.getWinState(), atkArmy.getTarget(), attackPlayer.lord.getLordId(), defendPlayer.lord.getLordId(), defendPlayer.lord.getCamp());

            //上报数数(攻击方)
            EventDataUp.battle(attackPlayer.account, attackPlayer.lord, attacker, "atk", "fightStateDominate",
                    String.valueOf(WorldConstant.BATTLE_TYPE_HIS_REMAIN), String.valueOf(fightLogic.getWinState()),
                    attackPlayer.roleId, report.getRptPlayer().getAtkHeroList(), rlc.getCityId());
            //上报数数(防守方)
            EventDataUp.battle(defendPlayer.account, defendPlayer.lord, defender, "def", "fightStateDominate",
                    String.valueOf(WorldConstant.BATTLE_TYPE_HIS_REMAIN), String.valueOf(fightLogic.getWinState()),
                    attackPlayer.roleId, report.getRptPlayer().getDefHeroList(), rlc.getCityId());
            return fightLogic;
        } catch (Exception e) {
            LogUtil.error(String.format("cityId :%d, attack roleId :%d, army keyId :%d, defend roleId :%d, army keyId :%d, fightId :%d, fight error!!!",
                    rlc.getCityId(), attackPlayer.roleId, atkArmy.getKeyId(), defendPlayer.roleId, defArmy.getKeyId(), fightId), e);
        }
        return null;
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

    /**
     * 部队string
     *
     * @param army
     * @return
     */
    protected String armyHeroString(Army army) {
        if (CheckNull.isEmpty(army.getHero())) return "";
        return army.getHero().stream()
                .map(twoInt -> new StringBuilder().append(twoInt.getV1()).append("_").append(twoInt.getV2()))
                .collect(Collectors.joining(","));
    }

    /**
     * 连杀广播
     *
     * @param attackPlayer 进攻玩家
     */
    protected void continuousKillBroadcast(Player attackPlayer, int killCnt) {
        Integer chatId = Constant.DOMINATE_CHAT_KILL_BROADCAST_MAP.get(killCnt);
        if (Objects.nonNull(chatId)) {
            int camp = attackPlayer.getCamp();
            chatDataManager.sendSysChat(chatId, camp, 0, camp, attackPlayer.lord.getNick());
        }
    }

    /**
     * 撤回部队
     *
     * @param player
     * @param army
     * @param changeMap
     * @param nowSec
     * @param b
     */
    protected void retreatArmy(Player player, Army army, Map<Long, ChangeInfo> changeMap, DominateSideCity sideCity, int nowSec, boolean b) {
        try {
            Map<Integer, Integer> recoverMap = army.getRecoverMap();
            if (CheckNull.nonEmpty(recoverMap)) doRecoverArmy(player, army, recoverMap, changeMap);
            worldService.retreatArmy(player, army, nowSec);
            worldService.synRetreatArmy(player, army, nowSec);
            if (Objects.nonNull(sideCity))
                sideCity.removeHolder(player.roleId, army.getKeyId());
        } catch (Exception e) {
            LogUtil.error(String.format("retreat army occur exception,roleId=%s,army=%s", player.roleId, army), e);
        }
    }

    /**
     * armyCopy
     *
     * @param armyKeyId
     * @param army
     * @return
     */
    protected Army simpleArmyCopy(int armyKeyId, Army army) {
        Army copyArmy = new Army();
        copyArmy.setKeyId(armyKeyId);
        copyArmy.setLordId(army.getLordId());
        copyArmy.setType(army.getType());
        copyArmy.setSubType(army.getSubType());
        copyArmy.setTarget(army.getTarget());
        copyArmy.setTargetId(army.getTargetId());
        copyArmy.setTarLordId(army.getTarLordId());
        copyArmy.setBattleId(army.getBattleId());
        copyArmy.setBattleTime(army.getBeginTime());
        copyArmy.setState(army.getState());
        copyArmy.setDuration(army.getDuration());
        copyArmy.setEndTime(army.getEndTime());
        copyArmy.setOriginCity(army.getOriginCity());
        copyArmy.setOriginPos(army.getOriginPos());
        return copyArmy;
    }

    /**
     * 创建战报
     *
     * @param fightLogic
     * @param attackPlayer
     * @param defendPlayer
     * @param recoverMap
     * @param nowSec
     * @param changeMap
     * @return
     */
    protected CommonPb.Report.Builder createFightReport(FightLogic fightLogic, Player attackPlayer, Player defendPlayer, Map<Long, Integer> recoverMap, int nowSec, Map<Long, ChangeInfo> changeMap) {
        // 战斗记录
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        CommonPb.Record record = fightLogic.generateRecord();
        rpt.setResult(fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS);
        Lord atkLord = attackPlayer.lord;
        Lord defLord = defendPlayer.lord;
        Fighter attacker = fightLogic.getAttacker();
        Fighter defender = fightLogic.getDefender();
        //战斗双方信息
        rpt.setAttack(PbHelper.createRptMan(atkLord.getPos(), atkLord.getNick(), atkLord.getVip(), atkLord.getLevel()));
        rpt.setDefMan(PbHelper.createRptMan(defLord.getPos(), defLord.getNick(), defLord.getVip(), defLord.getLevel()));
        //战斗摘要
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, atkLord.getCamp(), atkLord.getNick(),
                atkLord.getPortrait(), attackPlayer.getDressUp().getCurPortraitFrame(), recoverMap.getOrDefault(attackPlayer.roleId, 0)));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, defLord.getCamp(), defLord.getNick(),
                defLord.getPortrait(), defendPlayer.getDressUp().getCurPortraitFrame(), recoverMap.getOrDefault(defendPlayer.roleId, 0)));
        //攻击、防守的将领
        for (Force force : attacker.forces) {
            CommonPb.RptHero rptHero = fightService.addExploitAndBuildRptHero(force, changeMap, AwardFrom.STATE_DOMINATE_FIGHT);
            if (rptHero != null) {
                rpt.addAtkHero(rptHero);
            }
        }
        for (Force force : defender.forces) {
            CommonPb.RptHero rptHero = fightService.addExploitAndBuildRptHero(force, changeMap, AwardFrom.STATE_DOMINATE_FIGHT);
            if (rptHero != null) {
                rpt.addDefHero(rptHero);
            }
        }
        // 回合战报
        rpt.setRecord(record);
        return worldService.createAtkPlayerReport(rpt.build(), nowSec); // 战报
    }

    /**
     * 返回死亡武将
     *
     * @param player
     * @param fighter
     * @param army
     * @param changeMap
     * @param recoverMap
     * @param nowSec
     */
    protected void subAndRetreatDeadHeroInArmy(Player player, Fighter fighter, Army army, Map<Long, ChangeInfo> changeMap, Map<Long, Integer> recoverMap, DominateSideCity sideCity, int nowSec) {
        // 通用损兵处理
        warService.subBattleHeroArm(fighter.forces, changeMap, AwardFrom.STATE_DOMINATE_FIGHT);
        // 记录将领累计损兵恢复
        Map<Integer, Integer> lostHpMap = army.getAndCreateIfAbsentTotalLostHpMap();
        Map<Integer, Integer> recoverHpMap = army.getAndCreateIfAbsentRecoverMap();
        fighter.forces.stream().filter(force -> force.totalLost > 0).forEach(force -> {
            int totalLost = force.totalLost + lostHpMap.getOrDefault(force.id, 0);
            lostHpMap.put(force.id, totalLost);
            if (getWorldMapFunction() == WorldPb.WorldFunctionDefine.STATES_AND_COUNTIES_DOMINATE_VALUE) {
                int recoveryHp = (int) (Constant.STATE_DOMINATE_WORLD_MAP_RETURNING_SOLDIERS_RATIO / NumberUtil.TEN_THOUSAND_DOUBLE * totalLost);
                recoverHpMap.put(force.id, recoveryHp);
            }
        });
        //分离死亡部队和存活部队. 死亡将领回家, 存活将领继续战斗.
        List<CommonPb.TwoInt> deadHero = null, survivorHero = null;
        for (CommonPb.TwoInt twoInt : army.getHero()) {
            Hero hero = player.heros.get(twoInt.getV1());
            if (Objects.isNull(hero)) return;
            if (hero.getCount() <= 0) {
                if (Objects.isNull(deadHero)) deadHero = new ArrayList<>();
                deadHero.add(PbHelper.createTwoIntPb(twoInt.getV1(), 0));
            } else {
                if (Objects.isNull(survivorHero)) survivorHero = new ArrayList<>();
                survivorHero.add(PbHelper.createTwoIntPb(twoInt.getV1(), hero.getCount()));
            }
        }
        if (CheckNull.nonEmpty(survivorHero)) {
            //重新设置剩余存活将领
            army.setHero(survivorHero);
            //死亡的将领回家
            if (Objects.nonNull(deadHero)) {
                Army deadArmy = simpleArmyCopy(player.maxKey(), army);
                player.armys.put(deadArmy.getKeyId(), deadArmy);
                deadArmy.setHero(deadHero);
                Map<Integer, Integer> deadArmyRecoverMap = deadArmy.getAndCreateIfAbsentRecoverMap();
                for (CommonPb.TwoInt twoInt : deadHero) {
                    int recoverHp = recoverHpMap.getOrDefault(twoInt.getV1(), 0);
                    if (recoverHp > 0) deadArmyRecoverMap.put(twoInt.getV1(), recoverHp);
                    recoverMap.merge(player.roleId, recoverHp, Integer::sum);
                }
                //返回死亡将领
                retreatArmy(player, deadArmy, changeMap, sideCity, nowSec, false);
            }
        } else {
            //整个部队全部死亡
            army.setHero(deadHero);
            if (CheckNull.nonEmpty(deadHero)) {
                for (CommonPb.TwoInt twoInt : deadHero) {
                    int recoverHp = recoverHpMap.getOrDefault(twoInt.getV1(), 0);
                    recoverMap.merge(player.roleId, recoverHp, Integer::sum);
                }
            }
        }
    }

    /**
     * 没有防守将领, 直接占领
     *
     * @param sideCity
     * @param attackPlayer
     * @param army
     * @param nowSec
     */
    protected void handAttackDominateSuccess(DominateSideCity sideCity, Player attackPlayer, Army army, int nowSec) {
        //更换占领阵营，先计算原阵营的占领时间
        sideCity.changeCampHolder(attackPlayer.getCamp(), nowSec);

        List<Integer> posList = new ArrayList<>();
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(sideCity.getCityId());
        if (Objects.nonNull(staticCity)) {
            posList.add(staticCity.getCityPos());
            EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));
        }

        //玩家加入探索
        joinProbing(sideCity, attackPlayer, army, nowSec);

        // 占领遗迹后重置连杀次数
        afterOccupation(sideCity, attackPlayer);
        // 同步通知城池归属变更
        EventBus.getDefault().post(new Events.SyncDominateWorldMapChangeEvent(getWorldMapFunction()));
    }

    /**
     * 加入探索
     *
     * @param sideCity
     * @param player
     * @param army
     * @param now
     */
    protected void joinProbing(DominateSideCity sideCity, Player player, Army army, int now) {
        army.setState(getDefenceArmyType(getWorldMapPlay(getWorldMapFunction())));
        // TODO 修改主副武将将领状态


        sideCity.joinDefendList(new Turple<>(player.roleId, army.getKeyId()));
        sideCity.joinHoldArmy(player.roleId, army.getKeyId(), System.currentTimeMillis());
        LinkedList<Turple<Long, Integer>> defendList = sideCity.getDefendList();
        LogUtil.debug(String.format("dominate player join ,cityId :%d, roleId :%d, army keyId :%d, join defend list current defend list size :%d",
                sideCity.getCityId(), player.roleId, army.getKeyId(), defendList.size()));
        if (defendList.size() > Constant.MAXIMUM_NUMBER_OF_DOMINATE_DEFENSE_QUEUE) {
            LogUtil.warn(String.format("dominate player count,cityId :%d, roleId :%d, army keyId :%d, current defend list size :%d > %d",
                    sideCity.getCityId(), player.roleId, army.getKeyId(), defendList.size(), Constant.MAXIMUM_NUMBER_OF_DOMINATE_DEFENSE_QUEUE));
        }
    }

    /**
     * 伤兵恢复
     *
     * @param player
     * @param army
     * @param recoverMap
     * @param changeMap
     */
    protected abstract void doRecoverArmy(Player player, Army army, Map<Integer, Integer> recoverMap, Map<Long, ChangeInfo> changeMap);

    /**
     * 进攻成功
     *
     * @param fightLogic
     * @param sideCity
     * @param report
     * @param attackPlayer
     * @param army
     * @param defendPlayer
     * @param nowSec
     */
    protected abstract void attackFightSuccess(FightLogic fightLogic, DominateSideCity sideCity, CommonPb.Report.Builder report, Player attackPlayer, Army army,
                                    Player defendPlayer, int nowSec);

    /**
     * 进攻失败
     *
     * @param fightLogic
     * @param sideCity
     * @param report
     * @param attackPlayer
     * @param army
     * @param defendPlayer
     * @param nowSec
     */
    protected abstract void attackFightFailure(FightLogic fightLogic, DominateSideCity sideCity, CommonPb.Report.Builder report, Player attackPlayer, Army army,
                                    Player defendPlayer, int nowSec);

    /**
     * 占领城池之后
     *
     * @param sideCity
     * @param attackPlayer
     */
    protected abstract void afterOccupation(DominateSideCity sideCity, Player attackPlayer);
}
