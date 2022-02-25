package com.gryphpoem.game.zw.gameplay.local.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.gameplay.local.world.*;
import com.gryphpoem.game.zw.gameplay.local.world.army.*;
import com.gryphpoem.game.zw.gameplay.local.world.battle.BaseMapBattle;
import com.gryphpoem.game.zw.gameplay.local.world.battle.MapWarData;
import com.gryphpoem.game.zw.gameplay.local.world.map.*;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.GlobalWarFire;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.*;
import com.gryphpoem.game.zw.pb.SerializePb.*;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.DbCrossMap;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.Guard;
import com.gryphpoem.game.zw.resource.pojo.world.AirshipWorldData;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName CrossMapSerHelper.java
 * @Description 地图序列化和反序列化
 * @date 2019年4月3日
 */
public class CrossMapSerHelper {

    /**
     * 反序列化
     *
     * @param dbMap
     * @param cMap
     * @throws InvalidProtocolBufferException
     */
    public static void loadFromDbCrossMap(DbCrossMap dbMap, CrossWorldMap cMap) throws InvalidProtocolBufferException {
        if (dbMap.getMapId() != cMap.getMapId()) return;
        dserMapEntity(cMap, dbMap);
        dserArmy(cMap, dbMap);
        dserBattle(cMap, dbMap);
        dserMapInfo(cMap, dbMap);
        dserMapExt1(cMap, dbMap);

    }

    private static void dserMapExt1(CrossWorldMap cMap, DbCrossMap dbMap) throws InvalidProtocolBufferException {
        if (dbMap.getMapExt1() == null) return;
        SerMapExt1Pb serMapExt1Pb = SerMapExt1Pb.parseFrom(dbMap.getMapExt1());
        List<Airship> airshipPbList = serMapExt1Pb.getAirshipList();
        MapEntityGenerator mapEntityGenerator = cMap.getMapEntityGenerator();
        for (Airship airshipPb : airshipPbList) {
            AirshipWorldData airshipWorldData = new AirshipWorldData(airshipPb);
            mapEntityGenerator.getAirshipMap().put(airshipWorldData.getKeyId(), airshipWorldData);
            // 添加到地图上
            int pos = airshipWorldData.getPos();
            if (pos != -1 && airshipWorldData.isLiveStatus() && !cMap.getAllMap().containsKey(pos)) {
                AirshipMapEntity airshipMapEntity = new AirshipMapEntity(airshipWorldData.getPos(), airshipWorldData);
                cMap.addWorldEntity(airshipMapEntity);
            }
        }
        List<AreaSafe> safeList = serMapExt1Pb.getSafeList();
        for (AreaSafe areaSafe : safeList) {
            WFSafeAreaMapEntity entity = new WFSafeAreaMapEntity(areaSafe.getPos());
            entity.setCamp(areaSafe.getCamp());
            entity.setCellId(areaSafe.getCellId());
            entity.setSafeId(areaSafe.getSafeId());
            cMap.addWorldEntity(entity);
        }
    }

    private static void dserMapInfo(CrossWorldMap cMap, DbCrossMap dbMap) throws InvalidProtocolBufferException {
        if (dbMap.getMapInfo() == null) return;
        SerMapInfoPb serMapInfoPb = SerMapInfoPb.parseFrom(dbMap.getMapInfo());
        int mapOpenType = serMapInfoPb.getMapOpenType();
        MapOpenType openType = MapOpenType.getById(mapOpenType);
        cMap.setMapOpenType(openType);
        if (serMapInfoPb.hasWarPlanInfo()) {
            WorldWarPlanInfoPb warPlanInfoPb = serMapInfoPb.getWarPlanInfo();
            WorldWarPlanInfo planInfo = WorldWarPlanInfo.createByPb(warPlanInfoPb);
            cMap.getWorldWarOpen().setWorldWarPlanInfo(planInfo);
        }
        if (serMapInfoPb.hasWfPlanInfo()) {
            WarFirePlanInfoPb wfPlanInfo = serMapInfoPb.getWfPlanInfo();
            cMap.getGlobalWarFire().deSer(wfPlanInfo);
        }
    }

    private static void dserBattle(CrossWorldMap cMap, DbCrossMap dbMap) throws InvalidProtocolBufferException {
        if (dbMap.getBattle() == null) return;
        SerMapBattlePb serBattlePb = SerMapBattlePb.parseFrom(dbMap.getBattle());
        MapWarData mapWarData = cMap.getMapWarData();
        for (CommonPb.BattlePO battlePO : serBattlePb.getBattleList()) {
            BaseMapBattle baseMapBattle = battlePOToBaseBattle(battlePO);
            if (baseMapBattle == null) continue;
            mapWarData.addBattle(baseMapBattle);
            afterAddBattle(baseMapBattle, cMap);
        }
    }

    private static void afterAddBattle(BaseMapBattle baseMapBattle, CrossWorldMap cMap) {
        Battle battle = baseMapBattle.getBattle();
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        battle.setSponsor(playerDataManager.getPlayer(battle.getSponsorId()));
        battle.setDefencer(playerDataManager.getPlayer(battle.getDefencerId()));
        if (battle.getBattleId() > Battle.battleKey) {
            Battle.battleKey = battle.getBattleId();
        }
    }

    public static BaseMapBattle battlePOToBaseBattle(CommonPb.BattlePO battlePO) {
        Battle battle = new Battle(battlePO);
        BaseMapBattle baseMapBattle = BaseMapBattle.mapBattleFactory(battle);
        return baseMapBattle;
    }

    private static void dserArmy(CrossWorldMap cMap, DbCrossMap dbMap) throws InvalidProtocolBufferException {
        if (dbMap.getPlayerArmy() != null) {
            SerPlayerArmyPb serArmyPb = SerPlayerArmyPb.parseFrom(dbMap.getPlayerArmy());
            List<CommonPb.Army> armyPbList = serArmyPb.getArmyList();
            MapMarch mapMarchArmy = cMap.getMapMarchArmy();
            PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
            Map<Long, Player> allPlayers = playerDataManager.getPlayers();
            for (CommonPb.Army armyPb : armyPbList) {
                long roleId = armyPb.getLordId();
                Player armyPlayer = allPlayers.get(roleId);
                if (armyPlayer == null) continue;
                BaseArmy baseArmy = dbArmyToBaseArmy(armyPb);
                if (baseArmy == null) continue;
                mapMarchArmy.addArmy(baseArmy);
                afterAddArmy(armyPlayer, baseArmy, cMap);
            }
        }
    }

    private static void afterAddArmy(Player armyPlayer, BaseArmy baseArmy, CrossWorldMap cMap) {
        if (baseArmy instanceof CollectArmy) {
            if (baseArmy.getState() == ArmyConstant.ARMY_STATE_COLLECT) { // 采集的过程中,添加到矿点
                int tarPos = baseArmy.getTargetPos();
                BaseWorldEntity baseWorldEntity = cMap.getAllMap().get(tarPos);
                if (baseWorldEntity != null && baseWorldEntity.getType() == WorldEntityType.MINE) {
                    MineMapEntity mineEntity = (MineMapEntity) baseWorldEntity;
                    Guard guard = new Guard(armyPlayer, baseArmy.getArmy());
                    mineEntity.setGuard(guard);
                }
            }
        } else if (baseArmy instanceof AttackWFCityArmy) {
            int tarPos = baseArmy.getTargetPos();
            BaseWorldEntity baseWorldEntity = cMap.getAllMap().get(tarPos);
            if (Objects.nonNull(baseWorldEntity) && baseWorldEntity.getType() == WorldEntityType.CITY) {
                WFCityMapEntity wfCityMapEntity = (WFCityMapEntity) baseWorldEntity;
                wfCityMapEntity.getQueue().add((AttackWFCityArmy) baseArmy);
            }
        }
    }

    public static BaseArmy dbArmyToBaseArmy(CommonPb.Army armyPb) {
        Army army = new Army(armyPb);
        BaseArmy baseArmy = BaseArmy.baseArmyFactory(army);
        return baseArmy;
    }

    private static void dserMapEntity(CrossWorldMap cMap, DbCrossMap dbMap) throws InvalidProtocolBufferException {
        // 城池
        if (dbMap.getCity() != null) {
            SerMapEntityPb cityPb = SerMapEntityPb.parseFrom(dbMap.getCity());
            List<MapEntityPb> mapEntityList = cityPb.getMapEntityList();
            for (MapEntityPb pb : mapEntityList) {
                BaseWorldEntity entity = mapEntityPbToBaseWorldEntity(pb);
                if (entity != null && entity.getType() == WorldEntityType.CITY) {
                    CityMapEntity cityMapEntity = (CityMapEntity) entity;
                    StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityMapEntity.getCity().getCityId());
                    if (staticCity != null) {
                        cMap.addWorldEntityMultPos(cityMapEntity, staticCity.getPosList());
                        cMap.addCityMapEntity(cityMapEntity);
                    }
                }
            }
        }
        // 流寇
        if (dbMap.getBandit() != null) {
            SerMapEntityPb banditPb = SerMapEntityPb.parseFrom(dbMap.getBandit());
            List<MapEntityPb> mapEntityList = banditPb.getMapEntityList();
            for (MapEntityPb pb : mapEntityList) {
                BaseWorldEntity entity = mapEntityPbToBaseWorldEntity(pb);
                if (entity != null) {
                    cMap.addWorldEntity(entity);
                }
            }
        }
        // 矿点
        if (dbMap.getMine() != null) {
            SerMapEntityPb minePb = SerMapEntityPb.parseFrom(dbMap.getMine());
            List<MapEntityPb> mapEntityList = minePb.getMapEntityList();
            for (MapEntityPb pb : mapEntityList) {
                BaseWorldEntity entity = mapEntityPbToBaseWorldEntity(pb);
                if (entity != null) {
                    cMap.addWorldEntity(entity);
                }
            }
        }

        // 玩家添加到地图
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        final int mapId = cMap.getMapId();
        playerDataManager.getAllPlayer().values().stream().filter(p -> p.lord.getArea() == mapId)
                .map(p -> new PlayerMapEntity(p.lord.getPos(), p)).forEach(cMap::addWorldEntity);

    }

    public static BaseWorldEntity mapEntityPbToBaseWorldEntity(MapEntityPb pb) {
        BaseMapEntiyPb base = pb.getBase();
        int pos = base.getPos();
        int cfgId = base.getCfgId();
        int typeId = base.getEntityType();
        WorldEntityType worldEntityType = WorldEntityType.getWorldEntityTypeByTypeId(typeId);
        BaseWorldEntity worldEntity = null;
        if (worldEntityType == WorldEntityType.CITY) {
            CommonPb.City cityPb = pb.getCity();
            City city = new City(cityPb);
            WarFireMapEntityExt cityExt = pb.getCityExt();
            if (Objects.isNull(cityExt)) {
                worldEntity = new CityMapEntity(pos, city);
            } else {
                worldEntity = new WFCityMapEntity(pos, city);
                WFCityMapEntity wfCityMapEntity = (WFCityMapEntity) worldEntity;
                wfCityMapEntity.setOccupyTime(cityExt.getOccupyTime());
                wfCityMapEntity.setStatus(cityExt.getStatus());
                wfCityMapEntity.setLastScoreTime(cityExt.getLastScoreTime());
                wfCityMapEntity.setFirstBloodCamp(cityExt.getFirstBloodCamp());
                if (!CheckNull.isEmpty(cityExt.getFirstBloodNickList())) {
                    cityExt.getFirstBloodNickList().forEach(nick -> wfCityMapEntity.getFirstBloodRoleNames().add(nick));
                }
                List<TwoInt> statueList = cityExt.getStatueList();
                if (!CheckNull.isEmpty(statueList)) {
                    wfCityMapEntity.setStatusMap(statueList.stream().collect(Collectors.toMap(TwoInt::getV1, TwoInt::getV2, (oldV, newV) -> newV)));
                }
            }
        } else if (worldEntityType == WorldEntityType.BANDIT) {
            worldEntity = new BanditMapEntity(pos, cfgId);
        } else if (worldEntityType == WorldEntityType.MINE) {
            MapMinePb minePb = pb.getMine();
            if (minePb.getRemainRes() > 0) {
                WarFireMapEntityExt mineExt = pb.getCityExt();
                if (Objects.isNull(mineExt)) {
                    worldEntity = new MineMapEntity(pos, cfgId, minePb.getLv(), minePb.getMineType(),
                            minePb.getRemainRes());
                } else {
                    WFMineMapEntity wfm = new WFMineMapEntity(pos, cfgId, minePb.getLv(), minePb.getMineType(), minePb.getRemainRes());
                    wfm.setLastScoreTime(mineExt.getLastScoreTime());
                    worldEntity = wfm;
                }
            }


        }
        return worldEntity;
    }

    /*------------------------------------------序列化----------------------------------------*/

    /**
     * 序列化
     *
     * @param cMap
     * @return
     */
    public static DbCrossMap toDbCrossMap(CrossWorldMap cMap) {
        DbCrossMap dbMap = new DbCrossMap();
        dbMap.setMapId(cMap.getMapId());
        serMapEntity(cMap, dbMap);
        serArmy(cMap, dbMap);
        serBattle(cMap, dbMap);
        serMapInfo(cMap, dbMap);
        serMapExt1(cMap, dbMap);
        return dbMap;
    }

    // 预留字段
    private static void serMapExt1(CrossWorldMap cMap, DbCrossMap dbMap) {
        SerMapExt1Pb.Builder builder = SerMapExt1Pb.newBuilder();
        for (AirshipWorldData airship : cMap.getMapEntityGenerator().getAirshipMap().values()) {
            builder.addAirship(PbHelper.createAirshipDBPb(airship));
        }
        cMap.getSafeArea().forEach(safe -> builder.addSafe(safe.toAreaSafePb()));
        dbMap.setMapExt1(builder.build().toByteArray());
    }

    private static void serMapInfo(CrossWorldMap cMap, DbCrossMap crossMap) {
        SerMapInfoPb.Builder builder = SerMapInfoPb.newBuilder();
        WorldWarOpen worldWarOpen = cMap.getWorldWarOpen();
        WorldWarPlanInfo worldWarPlanInfo = worldWarOpen.getWorldWarPlanInfo();
        if (worldWarPlanInfo != null) {
            builder.setWarPlanInfo(WorldWarPlanInfo.toWorldWarPlanInfoPb(worldWarPlanInfo));
        }
        GlobalWarFire globalWarFire = cMap.getGlobalWarFire();
        if (Objects.nonNull(globalWarFire)) {
            builder.setWfPlanInfo(GlobalWarFire.toWarFireInfoPb(globalWarFire, null));
        }
        builder.setMapOpenType(cMap.getMapOpenType().getId());
        crossMap.setMapInfo(builder.build().toByteArray());
    }

    private static void serBattle(CrossWorldMap cMap, DbCrossMap dbMap) {
        SerMapBattlePb.Builder builder = SerMapBattlePb.newBuilder();
        MapWarData mapWarData = cMap.getMapWarData();
        for (BaseMapBattle baseMapBattle : mapWarData.getAllBattles().values()) {
            Battle battle = baseMapBattle.getBattle();
            builder.addBattle(PbHelper.createBattlePOPb(battle));
        }
        dbMap.setBattle(builder.build().toByteArray());
    }

    private static void serArmy(CrossWorldMap cMap, DbCrossMap dbMap) {
        SerPlayerArmyPb.Builder builder = SerPlayerArmyPb.newBuilder();
        MapMarch mapMarchArmy = cMap.getMapMarchArmy();
        for (PlayerArmy playerArmy : mapMarchArmy.getPlayerArmyMap().values()) {
            for (BaseArmy baseArmy : playerArmy.getArmy().values()) {
                builder.addArmy(PbHelper.createArmyPb(baseArmy.getArmy(), true));
            }
        }
        dbMap.setPlayerArmy(builder.build().toByteArray());
    }

    /**
     * 序列化MapEntity
     *
     * @param dbMap
     */
    private static void serMapEntity(CrossWorldMap cMap, DbCrossMap dbMap) {
        SerMapEntityPb.Builder cityEntity = SerMapEntityPb.newBuilder();
        SerMapEntityPb.Builder mineEntity = SerMapEntityPb.newBuilder();
        SerMapEntityPb.Builder banditEntity = SerMapEntityPb.newBuilder();
        for (Iterator<Entry<Integer, BaseWorldEntity>> it = cMap.getAllMap().entrySet().iterator(); it.hasNext(); ) {
            Entry<Integer, BaseWorldEntity> entry = it.next();
            Integer pos = entry.getKey();
            BaseWorldEntity entity = entry.getValue();
            if (entity.getType() == WorldEntityType.CITY) {
                if (pos == entity.getPos()) {
                    cityEntity.addMapEntity(entity.toDbData());
                }
            } else if (entity.getType() == WorldEntityType.MINE) {
                mineEntity.addMapEntity(entity.toDbData());
            } else if (entity.getType() == WorldEntityType.BANDIT) {
                banditEntity.addMapEntity(entity.toDbData());
            }
        }
        dbMap.setCity(cityEntity.build().toByteArray());
        dbMap.setMine(mineEntity.build().toByteArray());
        dbMap.setBandit(banditEntity.build().toByteArray());
    }
}
