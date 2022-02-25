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
 * @date 2019年3月21日
 */
public abstract class BaseMapBattle implements DelayRun {

    // 战斗的数据
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
        /** 未知 ,不用处理 */
        UNKNOW,
        /** 防守方迁城而取消 */
        DEFMOVECITY,
        /** 发起方取消 */
        ATKCANCEL,
        /** 玩家已经被击飞 */
        DEF_HITFLY,
    }

    /**
     * battle添加战斗
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
        // 柏林争霸攻击防守阵营特殊处理
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
     * 创建battle的工厂方法
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
            // 处理战斗逻辑
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
     * 开战
     * 
     * @param now
     * @param mapWarData
     */
    public abstract void doFight(int now, MapWarData mapWarData);

    /**
     * 返回正在去的路上的部队
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
     * 返回某个战斗的所参与的所有部队
     * 
     * @param mapWarData
     * @param baseMapBattle
     * @return 地图通知事件
     */
    public static List<MapEvent> returnArmyBattle(MapWarData mapWarData, BaseMapBattle baseMapBattle) {
        Battle ba = baseMapBattle.getBattle();
        List<BattleRole> atkList = ba.getAtkList();
        List<BattleRole> defList = ba.getDefList();
        int size = atkList.size() + defList.size();
        List<BattleRole> roleList = new ArrayList<>(size);
        List<MapEvent> mapEvents = new ArrayList<>(size); // 地图事件
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
     * 取消该战斗,并且返回部队
     * 
     * @param mapWarData
     */
    public void cancelBattleAndReturnArmy(MapWarData mapWarData, CancelBattleType cancelType) {
        List<MapEvent> mapEvents = returnArmyBattle(mapWarData, this, cancelType); // 地图事件

        // 取消战斗
        mapWarData.cancelBattleById(getBattleId());
        onCancelBattleAfter(mapWarData, cancelType);

        // 地图推送
        mapEvents.add(MapEvent.mapEntity(battle.getPos(), MapCurdEvent.UPDATE));
        mapWarData.getCrossWorldMap().publishMapEvent(mapEvents);
    }

    /**
     * 取消该战斗,并且返回部队，不做地图推送
     *
     * @param mapWarData
     */
    public void cancelBattleAndReturnArmyNoPush(MapWarData mapWarData, CancelBattleType cancelType) {
        // 地图事件
        returnArmyBattle(mapWarData, this, cancelType);
        // 取消战斗
        mapWarData.cancelBattleById(getBattleId());
        onCancelBattleAfter(mapWarData, cancelType);
    }

    /**
     * 取消城战之后执行
     * 
     * @param mapWarData
     */
    protected void onCancelBattleAfter(MapWarData mapWarData, CancelBattleType cancelType) {

    }

    /**
     * 获取战斗的防守兵力
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
            // 重新计算玩家防守方的兵力
            Player defencer = battle.getDefencer();
            try {
                playerDataManager.autoAddArmy(defencer);
                wallService.processAutoAddArmy(defencer);
            } catch (Exception e) {
                LogUtil.error("获取Battle补兵出错", e);
            }
            // 空闲上阵将领,和城防军
            List<Hero> heroList = defencer.getDefendHeros();
            for (Hero hero : heroList) {
                if (hero.getCount() > 0) {
                    realDefArm += hero.getCount();
                }
            }
            // 城防NPC
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
            // 驻守本城的其他玩家将领

            // 别人来帮助的兵力(已经达到目的地了)
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
            // 别人来帮助的正在路上的兵力
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
     * 防守方是人的情况,将城防NPC,驻防部队加入都battle中
     * 
     * @param mapWarData
     */
    public void addDefendRoleHeros(MapWarData mapWarData) {
        Player defencer = battle.getDefencer();
        if (defencer == null) return;
        // 自动补兵
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
        LogUtil.debug(defencer.lord.getPos() + "自己部队驻防信息=" + heroList + ",防守方Defs=" + battle.getDefList());
        int now = TimeHelper.getCurrentSecond();
        // 城防NPC
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
                wallNpc.setAddTime(now); // 刷新一下补兵的时间
                battle.getDefList().add(BattleRole.newBuilder().setKeyId(WorldConstant.ARMY_TYPE_WALL_NPC)
                        .setRoleId(defencer.roleId).addHeroId(ks.getKey()).build());
            }
        }

        // 驻守本城的其他玩家将领
        CrossWorldMap cmap = mapWarData.getCrossWorldMap();
        BaseWorldEntity baseWorldEntity = cmap.getAllMap().get(defencer.lord.getPos());
        if (baseWorldEntity != null && baseWorldEntity.getType() == WorldEntityType.PLAYER) {
            PlayerMapEntity playerMapEntity = (PlayerMapEntity) baseWorldEntity;
            List<Guard> helpGuradList = playerMapEntity.getHelpGurad();
            for (Guard guard : helpGuradList) {
                List<Integer> helpHeroIds = guard.getArmy().getHero().stream().map(h -> h.getV1())
                        .collect(Collectors.toList());
                // 防止防守成功,守军被撤回(撤回时会判断有部队ID的)
                battle.getDefList().add(BattleRole.newBuilder().setKeyId(WorldConstant.ARMY_TYPE_HLEP)
                        .setRoleId(guard.getPlayer().roleId).addAllHeroId(helpHeroIds).build());
            }
        }
    }

    public void joinBattle(AttackParamDto param) throws MwException {
    }
}
