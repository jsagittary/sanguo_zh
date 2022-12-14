package com.gryphpoem.game.zw.gameplay.local.world.battle;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
import com.gryphpoem.game.zw.gameplay.local.util.DelayRun;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.util.dto.AttackParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.army.BaseArmy;
import com.gryphpoem.game.zw.gameplay.local.world.army.MapMarch;
import com.gryphpoem.game.zw.gameplay.local.world.army.PlayerArmy;
import com.gryphpoem.game.zw.gameplay.local.world.map.BaseWorldEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.PlayerMapEntity;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.CommonPb.BattleRole;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.Constant.AttrId;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.WallNpc;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticWallHeroLv;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.Guard;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.WallService;
import com.gryphpoem.game.zw.service.WarService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @ClassName BaseMapBattle.java
 * @Description
 * @author QiuKun
 * @date 2019???3???21???
 */
public abstract class BaseMapBattle implements DelayRun {

    // ???????????????
    protected final Battle battle;

    public BaseMapBattle(Battle battle) {
        this.battle = battle;
    }

    public int getBattleId() {
        return battle.getBattleId();
    }

    @Override
    public int deadlineTime() {
        return battle.getBattleTime();
    }

    public Battle getBattle() {
        return battle;
    }

    public static enum CancelBattleType {
        /** ?????? ,???????????? */
        UNKNOW,
        /** ???????????????????????? */
        DEFMOVECITY,
        /** ??????????????? */
        ATKCANCEL,
        /** ????????????????????? */
        DEF_HITFLY,
    }

    /**
     * battle????????????
     * 
     * @param army
     */
    public void addBattleArmy(CrossWorldMap cmap, BaseArmy army) {
        if (army == null) return;
        Player armyPlayer = army.checkAndGetAmryHasPlayer(cmap.getMapMarchArmy());
        if (armyPlayer == null) return;
        int camp = armyPlayer.lord.getCamp();
        List<Integer> heroList = army.getArmy().getHero().stream().map(h -> h.getV1()).collect(Collectors.toList());
        BattleRole battleRole = BattleRole.newBuilder().setKeyId(army.getKeyId()).setRoleId(armyPlayer.roleId)
                .addAllHeroId(heroList).build();
        // ??????????????????????????????????????????
        if (battle.getType() == WorldConstant.BATTLE_TYPE_NEW_YORK_WAR) {
            if (camp == battle.getDefCamp()) {
                battle.getDefList().add(battleRole);
            } else {
                battle.getAtkList().add(battleRole);
            }
        } else {
            if (camp == battle.getAtkCamp()) {
                battle.getAtkList().add(battleRole);
            } else {
                battle.getDefList().add(battleRole);
            }
        }
    }

    /**
     * ??????battle???????????????
     * 
     * @param battle
     * @return
     */
    public static BaseMapBattle mapBattleFactory(Battle battle) {
        if (battle.getType() == WorldConstant.BATTLE_TYPE_CITY) {
            return new AttackPlayerBattle(battle);
        } else if (battle.getType() == WorldConstant.BATTLE_TYPE_CAMP) {
            return new AttackCityBattle(battle);
        } else if (battle.getType() == WorldConstant.BATTLE_TYPE_NEW_YORK_WAR) {
            return new NewYorkWarBattle(battle);
        }
        return null;
    }

    @Override
    public void deadRun(int runTime, DelayInvokeEnvironment env) {
        if (env instanceof MapWarData) {
            // ??????????????????
            MapWarData d = (MapWarData) env;
            try {
                doFight(runTime, d);
            } catch (Exception e) {
                LogUtil.error(e);
            }
            returnArmyBattle(d, this);
            d.finishlBattle(getBattleId());
        }
    }

    /**
     * ??????
     * 
     * @param now
     * @param mapWarData
     */
    public abstract void doFight(int now, MapWarData mapWarData);

    /**
     * ?????????????????????????????????
     * 
     * @param mapWarData
     * @param baseMapBattle
     * @return
     */
    public static List<MapEvent> returnArmyOnWayBattle(MapWarData mapWarData, BaseMapBattle baseMapBattle) {
        List<MapEvent> mapEvents = new ArrayList<>();
        Battle ba = baseMapBattle.getBattle();
        List<Long> roleIdList = new ArrayList<>();
        roleIdList.addAll(ba.getAtkRoles());
        roleIdList.addAll(ba.getDefRoles());
        final int battlePos = ba.getPos();
        for (Long roleId : roleIdList) {
            MapMarch mapMarchArmy = mapWarData.getCrossWorldMap().getMapMarchArmy();
            PlayerArmy playerArmy = mapMarchArmy.getPlayerArmyMap().get(roleId);
            if (playerArmy != null) {
                playerArmy.getArmy().values().stream().filter(baseArmy -> {
                    Army army = baseArmy.getArmy();
                    return army.getTarget() == battlePos && !army.isRetreat();
                }).forEach(baseArmy -> {
                    Player armyPlayer = baseArmy.checkAndGetAmryHasPlayer(mapMarchArmy);
                    if (armyPlayer != null) {
                        int marchTime = mapWarData.getCrossWorldMap().calcDistance(armyPlayer.lord.getPos(),
                                baseArmy.getTargetPos());
                        baseArmy.retreatArmy(mapMarchArmy, marchTime, marchTime);
                        mapEvents.add(baseArmy.createMapEvent(MapCurdEvent.UPDATE));
                    }
                });
            }
        }
        return mapEvents;
    }

    public static List<MapEvent> returnArmyBattle(MapWarData mapWarData, BaseMapBattle baseMapBattle,
            CancelBattleType cancelType) {
        List<MapEvent> mapEvents = returnArmyBattle(mapWarData, baseMapBattle);
        if (cancelType == CancelBattleType.DEFMOVECITY || cancelType == CancelBattleType.DEF_HITFLY
                || cancelType == CancelBattleType.ATKCANCEL) {
            mapEvents.addAll(returnArmyOnWayBattle(mapWarData, baseMapBattle));
        }
        return mapEvents;
    }

    /**
     * ?????????????????????????????????????????????
     * 
     * @param mapWarData
     * @param baseMapBattle
     * @return ??????????????????
     */
    public static List<MapEvent> returnArmyBattle(MapWarData mapWarData, BaseMapBattle baseMapBattle) {
        Battle ba = baseMapBattle.getBattle();
        List<BattleRole> atkList = ba.getAtkList();
        List<BattleRole> defList = ba.getDefList();
        int size = atkList.size() + defList.size();
        List<BattleRole> roleList = new ArrayList<>(size);
        List<MapEvent> mapEvents = new ArrayList<>(size); // ????????????
        roleList.addAll(atkList);
        roleList.addAll(defList);
        for (BattleRole role : roleList) {
            long roleId = role.getRoleId();
            int armyKeyId = role.getKeyId();
            MapMarch mapMarchArmy = mapWarData.getCrossWorldMap().getMapMarchArmy();
            BaseArmy baseArmy = mapMarchArmy.getBaseArmyByLordIdAndKeyId(roleId, armyKeyId);
            if (baseArmy != null) {
                Player armyPlayer = baseArmy.checkAndGetAmryHasPlayer(mapMarchArmy);
                if (armyPlayer != null && !baseArmy.getArmy().isRetreat()) {
                    int marchTime = mapWarData.getCrossWorldMap().calcDistance(armyPlayer.lord.getPos(),
                            baseArmy.getTargetPos());
                    baseArmy.retreatArmy(mapMarchArmy, marchTime, marchTime);
                    mapEvents.add(baseArmy.createMapEvent(MapCurdEvent.UPDATE));
                }
            }
        }
        return mapEvents;
    }

    /**
     * ???????????????,??????????????????
     * 
     * @param mapWarData
     */
    public void cancelBattleAndReturnArmy(MapWarData mapWarData, CancelBattleType cancelType) {
        List<MapEvent> mapEvents = returnArmyBattle(mapWarData, this, cancelType); // ????????????

        // ????????????
        mapWarData.cancelBattleById(getBattleId());
        onCancelBattleAfter(mapWarData, cancelType);

        // ????????????
        mapEvents.add(MapEvent.mapEntity(battle.getPos(), MapCurdEvent.UPDATE));
        mapWarData.getCrossWorldMap().publishMapEvent(mapEvents);
    }

    /**
     * ???????????????,???????????????????????????????????????
     *
     * @param mapWarData
     */
    public void cancelBattleAndReturnArmyNoPush(MapWarData mapWarData, CancelBattleType cancelType) {
        // ????????????
        returnArmyBattle(mapWarData, this, cancelType);
        // ????????????
        mapWarData.cancelBattleById(getBattleId());
        onCancelBattleAfter(mapWarData, cancelType);
    }

    /**
     * ????????????????????????
     * 
     * @param mapWarData
     */
    protected void onCancelBattleAfter(MapWarData mapWarData, CancelBattleType cancelType) {

    }

    /**
     * ???????????????????????????
     * 
     * @param battle
     * @param cMap
     * @return
     */
    public static int getDefArmCntByBattle(Battle battle, CrossWorldMap cMap) {
        int defArm = battle.getDefArm();
        if (battle.getType() == WorldConstant.BATTLE_TYPE_CITY || battle.isRebellionBattle()
                || (battle.isCounterAtkBattle() && battle.getBattleType() == WorldConstant.COUNTER_ATK_ATK
                        || battle.isDecisiveBattle())) {
            PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
            WallService wallService = DataResource.ac.getBean(WallService.class);
            int realDefArm = 0;
            // ????????????????????????????????????
            Player defencer = battle.getDefencer();
            try {
                playerDataManager.autoAddArmy(defencer);
                wallService.processAutoAddArmy(defencer);
            } catch (Exception e) {
                LogUtil.error("??????Battle????????????", e);
            }
            // ??????????????????,????????????
            List<Hero> heroList = defencer.getDefendHeros();
            for (Hero hero : heroList) {
                if (hero.getCount() > 0) {
                    realDefArm += hero.getCount();
                }
            }
            // ??????NPC
            WallNpc wallNpc = null;
            if (!defencer.wallNpc.isEmpty()) {
                for (Entry<Integer, WallNpc> ks : defencer.wallNpc.entrySet()) {
                    wallNpc = ks.getValue();
                    StaticWallHeroLv staticSuperEquipLv = StaticBuildingDataMgr.getWallHeroLv(wallNpc.getHeroNpcId(),
                            wallNpc.getLevel());
                    int maxArmy = staticSuperEquipLv.getAttr().get(AttrId.LEAD);
                    if (wallNpc.getCount() < maxArmy) {
                        continue;
                    }
                    realDefArm += maxArmy;
                }
            }
            // ?????????????????????????????????

            // ????????????????????????(????????????????????????)
            for (BattleRole br : battle.getDefList()) {
                Player player = playerDataManager.getPlayer(br.getRoleId());
                if (player != null) {
                    List<Integer> heroIdList = br.getHeroIdList();
                    if (!CheckNull.isEmpty(heroIdList)) {
                        for (Integer heroId : heroIdList) {
                            StaticHero sHero = StaticHeroDataMgr.getHeroMap().get(heroId);
                            if (null == sHero) {
                                continue;
                            }
                            Hero hero = player.heros.get(heroId);
                            if (null == hero) {
                                continue;
                            }
                            if (hero.getCount() <= 0) {
                                continue;
                            }
                            realDefArm += hero.getCount();
                        }
                    }
                }
            }
            // ???????????????????????????????????????
            realDefArm += getDefArmyCountByCityBattle(battle, cMap);
            defArm = realDefArm;
        }
        return defArm;
    }

    private static int getDefArmyCountByCityBattle(Battle battle, CrossWorldMap cMap) {
        int cnt = 0;
        int pos = battle.getPos();
        for (Long rId : battle.getDefRoles()) {
            PlayerArmy playerArmy = cMap.getMapMarchArmy().getPlayerArmyMap().get(rId);
            if (playerArmy == null) {
                continue;
            }
            for (BaseArmy baseArmy : playerArmy.getArmy().values()) {
                Army army = baseArmy.getArmy();
                if (army.getTarget() == pos && army.getState() == ArmyConstant.ARMY_STATE_MARCH) {
                    for (TwoInt kv : army.getHero()) {
                        cnt += kv.getV2();
                    }
                }
            }
        }
        return cnt;
    }

    /**
     * ????????????????????????,?????????NPC,?????????????????????battle???
     * 
     * @param mapWarData
     */
    public void addDefendRoleHeros(MapWarData mapWarData) {
        Player defencer = battle.getDefencer();
        if (defencer == null) return;
        // ????????????
        WarService warService = DataResource.ac.getBean(WarService.class);
        warService.autoFillArmy(defencer);
        List<Hero> heroList = defencer.getDefendHeros();
        List<Integer> heroIdList = new ArrayList<>();
        for (Hero hero : heroList) {
            if (hero.getCount() <= 0) {
                continue;
            }
            heroIdList.add(hero.getHeroId());
        }
        battle.getDefList().add(BattleRole.newBuilder().setKeyId(WorldConstant.ARMY_TYPE_HLEP)
                .setRoleId(defencer.roleId).addAllHeroId(heroIdList).build());
        LogUtil.debug(defencer.lord.getPos() + "????????????????????????=" + heroList + ",?????????Defs=" + battle.getDefList());
        int now = TimeHelper.getCurrentSecond();
        // ??????NPC
        WallNpc wallNpc = null;
        if (!defencer.wallNpc.isEmpty()) {
            for (Entry<Integer, WallNpc> ks : defencer.wallNpc.entrySet()) {
                wallNpc = ks.getValue();
                StaticWallHeroLv staticSuperEquipLv = StaticBuildingDataMgr.getWallHeroLv(wallNpc.getHeroNpcId(),
                        wallNpc.getLevel());
                int maxArmy = staticSuperEquipLv.getAttr().get(AttrId.LEAD);
                if (wallNpc.getCount() < maxArmy) {
                    continue;
                }
                wallNpc.setAddTime(now); // ???????????????????????????
                battle.getDefList().add(BattleRole.newBuilder().setKeyId(WorldConstant.ARMY_TYPE_WALL_NPC)
                        .setRoleId(defencer.roleId).addHeroId(ks.getKey()).build());
            }
        }

        // ?????????????????????????????????
        CrossWorldMap cmap = mapWarData.getCrossWorldMap();
        BaseWorldEntity baseWorldEntity = cmap.getAllMap().get(defencer.lord.getPos());
        if (baseWorldEntity != null && baseWorldEntity.getType() == WorldEntityType.PLAYER) {
            PlayerMapEntity playerMapEntity = (PlayerMapEntity) baseWorldEntity;
            List<Guard> helpGuradList = playerMapEntity.getHelpGurad();
            for (Guard guard : helpGuradList) {
                List<Integer> helpHeroIds = guard.getArmy().getHero().stream().map(h -> h.getV1())
                        .collect(Collectors.toList());
                // ??????????????????,???????????????(???????????????????????????ID???)
                battle.getDefList().add(BattleRole.newBuilder().setKeyId(WorldConstant.ARMY_TYPE_HLEP)
                        .setRoleId(guard.getPlayer().roleId).addAllHeroId(helpHeroIds).build());
            }
        }
    }

    public void joinBattle(AttackParamDto param) throws MwException {
    }
}
