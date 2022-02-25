package com.gryphpoem.game.zw.gameplay.local.world.map;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.service.newyork.NewYorkWarService;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.util.dto.AttackParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.battle.BaseMapBattle;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.MapEntityPb.Builder;
import com.gryphpoem.game.zw.pb.GamePb5.*;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.WorldService;

import java.util.List;

/**
 * @author QiuKun
 * @ClassName CityMapEntity.java
 * @Description
 * @date 2019年3月20日
 */
public class CityMapEntity extends BaseWorldEntity {

    private final City city;

    public CityMapEntity(int pos, City city) {
        super(pos, WorldEntityType.CITY);
        this.city = city;
    }

    public City getCity() {
        return city;
    }

    @Override
    public Builder toDbData() {
        Builder dbData = super.toDbData();
        dbData.setCity(PbHelper.createCityPb(city));
        return dbData;
    }

    public CommonPb.AreaCity toAreaCityPb(Player invokePlayer, CrossWorldMap crossWorldMap) {
        Player cityOwer = getCityOwer(crossWorldMap);
        return PbHelper.createAreaCityPb(invokePlayer, city, cityOwer);
    }

    public CommonPb.MapCity toMapCityPb(Player invokePlayer, CrossWorldMap crossWorldMap) {
        StaticCity staticCity = StaticCrossWorldDataMgr.getCityMap().get(city.getCityId());
        if (staticCity == null) return null;
        Player cityOwer = getCityOwer(crossWorldMap);
        String ownerName = cityOwer == null ? null : cityOwer.lord.getNick();
        return PbHelper.createMapCityPb(invokePlayer, pos, city, staticCity, ownerName, null);
    }

    public Player getCityOwer(CrossWorldMap crossWorldMap) {
        if (city.getOwnerId() > 0) {
            PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
            Player player = playerDataManager.getPlayer(city.getOwnerId());
            if (player != null) {
                return player;
            }
        }
        city.setOwnerId(0);
        return null;
    }

    @Override
    public void attackPos(AttackParamDto param) throws MwException {
        int now = TimeHelper.getCurrentSecond();
        Player player = param.getInvokePlayer();
        long roleId = player.roleId;

        if (WorldConstant.ATTACK_STATE_NEED_LV > player.lord.getLevel()) {
            throw new MwException(GameError.ATTACK_STATE_NEED_LV.getCode(), "指挥官先磨砺至45级，再发动阵营战吧, roleId:", roleId);
        }
        int cityId = city.getCityId();
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        if (null == staticCity) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "发起阵营战，未找到城池, roleId:", roleId, ", cityId:",
                    cityId);
        }
        CrossWorldMap crossWorldMap = param.getCrossWorldMap();
        if (staticCity.getOpen() > crossWorldMap.getMapOpenType().getId()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "此城池还未开放, roleId:", roleId, ", cityId:", cityId,
                    ", curOpenId:", crossWorldMap.getMapOpenType().getId());
        }
        // 纽约争霸期间不可发送攻城战
        NewYorkWarService newYorkWarService = DataResource.ac.getBean(NewYorkWarService.class);
        if(cityId == CrossWorldMapConstant.NEW_YORK_CITY_ID){
            newYorkWarService.checkIsNewYorkWarTime();
        }
        // 大本营不能被攻击
        if (staticCity.getType() == CrossWorldMapConstant.CITY_TYPE_CAMP) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "新地图国都城池不能攻打, roleId:", roleId, ", cityId:", cityId);
        }
        // 攻打City的前置判断
        List<Integer> attackPrecondition = staticCity.getAttackPrecondition();
        if (!CheckNull.isEmpty(attackPrecondition)) {
            if (!checkAtkPreCond(attackPrecondition, player.lord.getCamp(), param)) {
                throw new MwException(GameError.ATK_CROSS_CAMP_BATTLE_ERROR.getCode(),
                        "发起阵营战, 未占领相邻所属城市，无法发起攻城战 cityId:", cityId);
            }
        }
        int myCamp = player.lord.getCamp();
        // 拥有城池数量
        if (city.getCamp() == Constant.Camp.NPC
                && city.getCityId() != CrossWorldMapConstant.NEW_YORK_CITY_ID
                && !checkAttkableTypeNpcCity(myCamp, crossWorldMap)) {
            // 检查本方阵营的名城是否多余10个,如果多余10个就不让发起
            throw new MwException(GameError.ATK_CROSS_OWN_MAX_CITY.getCode(), "本方阵营的名城数量超过上限不让发起战斗 roleId:", roleId);
        }

        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        WorldService worldService = DataResource.ac.getBean(WorldService.class);

        List<BaseMapBattle> battleList = crossWorldMap.getMapWarData().getBattlesByPos(staticCity.getCityPos());
        int camp = player.lord.getCamp();
        if (!CheckNull.isEmpty(battleList) && battleList.stream()
                .filter(baseMap -> baseMap.getBattle().getAtkCamp() == camp).findFirst().isPresent()) {
            throw new MwException(GameError.CAMP_BATTLE_HAS_EXISTS.getCode(), "发起阵营战，已有玩家发起对该城的阵营战, roleId:", roleId,
                    ", cityId:", cityId);
        }

        if (city.getCamp() > 0 && camp == city.getCamp()) {
            throw new MwException(GameError.SAME_CAMP.getCode(), "同阵营不允许,发起阵营战, roleId:", roleId, ", cityId:",
                    staticCity.getCityId());
        }

        if (city.getProtectTime() > now) {
            throw new MwException(GameError.CITY_PROTECT.getCode(), "城池保护中, roleId:", roleId, ", cityId:", cityId);
        }

        if (CheckNull.isEmpty(battleList)) {
            // 记录进攻该城池的阵营
            city.setAttackCamp(player.lord.getCamp());
            city.setStatus(WorldConstant.CITY_STATUS_BATTLE);
        }

        Battle battle = new Battle();
        battle.setType(WorldConstant.BATTLE_TYPE_CAMP);
        battle.setBattleTime(now + staticCity.getCountdown());
        battle.setPos(staticCity.getCityPos());
        battle.setSponsor(player);
        battle.setDefCamp(city.getCamp());
        battle.addDefArm(city.getCurArm());
        battle.getAtkRoles().add(roleId);
        battle.setAtkCamp(camp);
        if (city.getCamp() != Constant.Camp.NPC) {
            if (city.getOwnerId() > 0) {
                Player defencer = playerDataManager.getPlayer(city.getOwnerId());
                battle.setDefencer(defencer);
                worldService.pushAttackCamp(defencer.account, staticCity.getArea(), staticCity.getCityId(),
                        defencer.lord.getNick());
            }
        } else {
            battle.setAtkNpc(true);
        }

        // 添加Battle
        BaseMapBattle baseBattle = BaseMapBattle.mapBattleFactory(battle);
        crossWorldMap.getMapWarData().addBattle(baseBattle);

        // 通知阵营战信息
        worldService.syncAttackCamp(battle, cityId);
        // 地图推送
        crossWorldMap.publishMapEvent(MapEvent.mapEntity(battle.getPos(), MapCurdEvent.UPDATE),
                MapEvent.mapEntity(player.lord.getPos(), MapCurdEvent.UPDATE));

        AttackCrossPosRs.Builder builder = param.getBuilder();
        builder.setBattle(PbHelper.createBattlePb(battle));
    }

    /**
     * 中立城的判断
     * 
     * @param myCamp
     * @param crossWorldMap
     * @return
     */
    private boolean checkAttkableTypeNpcCity(int myCamp, CrossWorldMap crossWorldMap) {
        /**
         * 在世界争霸，每个阵营最多占领10个城市的规则中，不包含纽约
         * 即若某个阵营已占领了10个城市且未包含纽约，攻下纽约，可以占领纽约
         */
        int hasCityCnt = (int) crossWorldMap.getCityMap().values().stream()
                .filter(cityEntity -> cityEntity.getCity().getCityId() != CrossWorldMapConstant.NEW_YORK_CITY_ID)
                // 已占领特区个数
                .filter(cityEntity -> cityEntity.getCity().getCamp() == myCamp).count();
        // 已发起的其他阵营特区个数+已发起的中立城个数
        int battleCnt = 0;
        for (CityMapEntity cityMapEntity : crossWorldMap.getCityMap().values()) {
            List<BaseMapBattle> battlesByPos = crossWorldMap.getMapWarData().getBattlesByPos(cityMapEntity.getPos());
            if (!CheckNull.isEmpty(battlesByPos)) {
                int cnt = (int) battlesByPos.stream()
                        .filter(baseBattle -> baseBattle.getBattle().getAtkCity() != CrossWorldMapConstant.NEW_YORK_CITY_ID)
                        .filter(baseBattle -> baseBattle.getBattle().getAtkCamp() == myCamp).count();
                battleCnt += cnt;
            }
        }
        LogUtil.debug("检测当前阵营是否可以对名城发起进攻  battleCnt:", battleCnt, ", hasCityCnt:", hasCityCnt);
        return (battleCnt + hasCityCnt) < CrossWorldMapConstant.OWN_CITY_MAX_CNT;
    }

    /**
     * 攻打城池判断前置条件
     *
     * @param cityIdList 城池Id
     * @param camp 阵营
     * @param param 参数
     * @return false 不满足, true 满足
     */
    public boolean checkAtkPreCond(List<Integer> cityIdList, int camp, AttackParamDto param) {
        CrossWorldMap cMap = param.getCrossWorldMap();
        for (int cityId : cityIdList) {
            CityMapEntity mapEntity = cMap.getCityMap().get(cityId);
            if (mapEntity == null) {
                continue;
            }
            if (mapEntity.getCity().getCamp() == camp) {
                return true;
            }
        }
        return false;
    }

    public void clearStateToInit() {
        city.clearStateToInit();
    }

}
