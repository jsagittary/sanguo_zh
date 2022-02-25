package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.MapHelper;
import com.gryphpoem.game.zw.service.CampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @ClassName WarDataManager.java
 * @Description 城战、阵营战等数据管理类
 * @author TanDonghai
 * @date 创建时间：2017年4月12日 下午2:38:04
 *
 */
@Component
public class WarDataManager {
    @Autowired
    private GlobalDataManager globalDataManager;

    @Autowired
    private WorldDataManager worldDataManager;

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private CampService campService;

    /** 分坐标保存战斗信息, key:pos */
    private Map<Integer, LinkedList<Battle>> battlePosMap = new ConcurrentHashMap<>();

    /** key:battleId */
    private Map<Integer, Battle> battleMap = new ConcurrentHashMap<>();

    /** 存储特殊战斗对象,Example 闪电战(activityType:14) */
    private Map<Integer, Battle> specialBattleMap = new ConcurrentHashMap<>();

    /** 分区域保存战斗坐标, key:areaId, value:Set<pos> */
    private Map<Integer, Set<Integer>> battleAreaMap = new ConcurrentHashMap<>();

    /** 匪军攻城战斗缓存战斗 key:roleId,value:battleId */
    private Map<Long, Integer> rebelBattleCacheMap = new ConcurrentHashMap<>();

    public Map<Integer, Battle> getBattleMap() {
        return battleMap;
    }

    public Map<Integer, LinkedList<Battle>> getBattlePosMap() {
        return battlePosMap;
    }

    public Map<Integer, Set<Integer>> getBattleAreaMap() {
        return battleAreaMap;
    }

    public Map<Integer, Battle> getSpecialBattleMap() {
        return specialBattleMap;
    }

    public void setSpecialBattleMap(Map<Integer, Battle> specialBattleMap) {
        this.specialBattleMap = specialBattleMap;
    }

    public Battle removeBattleById(int battleId) {
        Battle battle = battleMap.remove(battleId);
        if (null != battle) {
            campService.syncCancelRallyBattle(null, battle, null);
            removePosBattleById(battle.getPos(), battleId);
        }
        return battle;
    }

    public Battle removeSuperMineBattleById(int battleId) {
        Battle battle = battleMap.remove(battleId);
        if (null != battle) {
            campService.syncCancelRallyBattle(null, battle, null);
        }
        return battle;
    }

    public Battle removeSpecialBattleById(int battleId) {
        Battle battle = specialBattleMap.remove(battleId);
        if (null != battle) {
            removePosBattleById(battle.getPos(), battleId);
        }
        return battle;
    }

    /**
     * 移除某个坐标点的某场战斗
     * 
     * @param pos
     * @param battleId
     */
    public void removePosBattleById(int pos, int battleId) {
        LinkedList<Battle> battleList = battlePosMap.get(pos);
        boolean rmBattle = true;
        if (!CheckNull.isEmpty(battleList)) {
            Iterator<Battle> its = battleList.iterator();
            while (its.hasNext()) {
                Battle battle = (Battle) its.next();
                if (battle.getBattleId() == battleId) {
                    its.remove();
                }
            }
            if (!CheckNull.isEmpty(battleList)) {
                rmBattle = false;
            }
        }

        if (rmBattle) {
            Set<Integer> posSet = battleAreaMap.get(MapHelper.getAreaIdByPos(pos));
            posSet.remove(pos);
            battlePosMap.remove(pos);
        }
    }

    /**
     * 服务器启动后，初始化战斗信息
     */
    public void initBattle() {
        int maxId = 0;
        battleMap = globalDataManager.getGameGlobal().getBattleMap();
        specialBattleMap = globalDataManager.getGameGlobal().getSpecialBattleMap();
        Iterator<Battle> its = battleMap.values().iterator();
        while (its.hasNext()) {
            Battle battle = its.next();
            putPosBattle(battle);

            battle.setSponsor(playerDataManager.getPlayer(battle.getSponsorId()));
            battle.setDefencer(playerDataManager.getPlayer(battle.getDefencerId()));
            parseBattleRole(battle);

            if (battle.getBattleId() > maxId) {
                maxId = battle.getBattleId();
            }
            // 初始化匪军叛乱的map数据 rebelBattleCacheMap
            rebelBattleCacheMap.put(battle.getDefencerId(), battle.getBattleId());
        }
        Iterator<Battle> iterator = specialBattleMap.values().iterator();
        while (iterator.hasNext()) {
            Battle battle = iterator.next();
            putPosBattle(battle);

            battle.setSponsor(playerDataManager.getPlayer(battle.getSponsorId()));
            battle.setDefencer(playerDataManager.getPlayer(battle.getDefencerId()));
            parseBattleRole(battle);

            if (battle.getBattleId() > maxId) {
                maxId = battle.getBattleId();
            }
        }
        Battle.battleKey = maxId;
    }

    public void parseBattleRole(Battle battle) {
        // Hero hero;
        // Player player;
        // if (!CheckNull.isEmpty(battle.getAtkList())) {
        // for (BattleRole role : battle.getAtkList()) {
        // player = playerDataManager.getPlayer(role.getRoleId());
        // // battle.getAtkRoleList().add(player);
        // for (Integer heroId : role.getHeroIdList()) {
        // hero = player.heros.get(heroId);
        // if (null != hero) {
        // battle.addAtkArm(hero.getCount());
        // }
        // }
        // }
        // }
        // if (!CheckNull.isEmpty(battle.getDefList())) {
        // for (BattleRole role : battle.getDefList()) {
        // player = playerDataManager.getPlayer(role.getRoleId());
        // // battle.getDefRoleList().add(player);
        // for (Integer heroId : role.getHeroIdList()) {
        // hero = player.heros.get(heroId);
        // if (null != hero) {
        // battle.addDefArm(hero.getCount());
        // }
        // }
        // }
        // }

        StaticCity staticCity = StaticWorldDataMgr.getCityByPos(battle.getPos());
        if (null != staticCity) {
            City city = worldDataManager.getCityById(staticCity.getCityId());
            battle.setDefCamp(city.getCamp());
            if (city.getCamp() == Constant.Camp.NPC) {
                battle.setAtkNpc(true);
            }
        } else {
            if (battle.getType() <= 0) return;
            if (battle.getType() != WorldConstant.BATTLE_TYPE_GESTAPO) {
                if (battle.getDefencer() != null) {
                    battle.setDefCamp(battle.getDefencer().lord.getCamp());
                }
            }
        }
    }

    /**
     * 移除坐标点上的所有城战信息
     * 
     * @param pos
     */
    public void removePosAllCityBattle(int pos) {
        LinkedList<Battle> list = battlePosMap.remove(pos);
        if (!CheckNull.isEmpty(list)) {
            for (Battle battle : list) {
                campService.syncCancelRallyBattle(null, battle, null);
                battleMap.remove(battle.getBattleId());
            }
        }

        Set<Integer> posSet = battleAreaMap.get(MapHelper.getAreaIdByPos(pos));
        posSet.remove(pos);
    }

    /**
     * 移除坐标点上的所有战斗信息, 并处理特殊战斗
     * @param oldPos
     * @param newPos
     */
    public void removePosExchangeSpecialBattle(int oldPos, int newPos) {
        LinkedList<Battle> list = battlePosMap.remove(oldPos);
        if (!CheckNull.isEmpty(list)) {
            for (Battle battle : list) {
                campService.syncCancelRallyBattle(null, battle, null);
                battleMap.remove(battle.getBattleId());
                specialBattleMap.remove(battle.getBattleId());
                if (battle.getType() == WorldConstant.BATTLE_TYPE_COUNTER_ATK
                        && battle.getBattleType() == WorldConstant.COUNTER_ATK_ATK && newPos > 0) { // 反攻德意志BOSS打玩家
                    battle.setPos(newPos);
                    addSpecialBattle(battle);
                    Player defencer = battle.getDefencer();
                    if (!CheckNull.isNull(defencer)) {
                        HashSet<Integer> set = defencer.battleMap.get(newPos);
                        if (set == null) {
                            set = new HashSet<>();
                            defencer.battleMap.put(newPos, set);
                        }
                        set.add(battle.getBattleId());
                    }
                }
            }
        }

        Set<Integer> posSet = battleAreaMap.get(MapHelper.getAreaIdByPos(oldPos));
        posSet.remove(oldPos);
    }

    /**
     * 添加战斗
     * @param battle
     */
    public void addBattle(Player player, Battle battle) {
        LogUtil.debug("添加战斗=" + battle);
        battleMap.put(battle.getBattleId(), battle);
        campService.syncRallyBattle(player, battle, null);
        putPosBattle(battle);
    }

    /**
     * 添加特殊战斗
     * @param specialBattle
     */
    public void addSpecialBattle(Battle specialBattle) {
        LogUtil.debug("添加战斗=" + specialBattle);
        specialBattleMap.put(specialBattle.getBattleId(), specialBattle);
        putPosBattle(specialBattle);
    }

    private void putPosBattle(Battle battle) {
        if (battle.isAtkSuperMine()) {// 超级矿点不加入分坐标点
            return;
        }
        if (battle.isRebellionBattle()) {// 匪军叛乱,加到匪军叛乱缓存map中
            rebelBattleCacheMap.put(battle.getDefLordId(), battle.getBattleId());
            return;
        }
        int pos = battle.getPos();
        LinkedList<Battle> battleList = battlePosMap.get(pos);
        if (null == battleList) {
            battleList = new LinkedList<>();
            battlePosMap.put(pos, battleList);
        }
        boolean sameBattle = false;
        for (Battle b : battleList) {
            if (b.getBattleId() == battle.getBattleId()) {
                sameBattle = true;
            }
        }
        if (!sameBattle) {
            battleList.add(battle);
        }
        int areaId = MapHelper.getAreaIdByPos(pos);
        Set<Integer> posSet = battleAreaMap.get(areaId);
        if (null == posSet) {
            posSet = new HashSet<>();
            battleAreaMap.put(areaId, posSet);
        }
        posSet.add(pos);
    }

    public boolean posHaveBattle(int pos) {
        LinkedList<Battle> battleList = battlePosMap.get(pos);
        return !CheckNull.isEmpty(battleList);
    }

    /**
     * 玩家有没有被反攻德意志BOSS打
     * @param player
     * @return
     */
    public boolean playerHaveCounterBattle(Player player) {
        List<Battle> battleList = specialBattleMap.values().stream() // 加上被反攻德意志BOSS进攻的军情
                .filter(battle -> battle.getType() == WorldConstant.BATTLE_TYPE_COUNTER_ATK
                        && battle.getBattleType() == WorldConstant.COUNTER_ATK_ATK && battle.getDefencer() != null
                        && battle.getDefencer().roleId == player.roleId).distinct().collect(Collectors.toList());
        return !CheckNull.isEmpty(battleList);
    }

    /**
     * 记录双方阵型(用于有城墙NPC)
     */
    public void packForm(CommonPb.Record.Builder recordData, List<Force> atkForces, List<Force> defForces) {
        Player player;
        CommonPb.Form.Builder form;
        for (Force force : atkForces) {
            if (force.hp <= 0) continue;
            form = CommonPb.Form.newBuilder();
            form.setId(force.id);
            form.setCount(force.hp);// force.maxHp
            form.setLine(force.maxLine);
            form.setCamp(force.getCamp());
            form.setHeroType(force.roleType);
            form.setCurLine(force.curLine);
            form.setIntensifyLv(force.getIntensifyLv() == 0 ? 1 : force.getIntensifyLv());
            player = playerDataManager.getPlayer(force.ownerId);
            if (player != null) {
                form.setOwnerId(player.lord.getNick());
            }
            recordData.addFormA(form);
        }

        for (Force force : defForces) {
            if (force.hp <= 0) continue;
            form = CommonPb.Form.newBuilder();
            form.setId(force.id);
            player = playerDataManager.getPlayer(force.ownerId);
            if (player != null && force.roleType == Constant.Role.WALL) {
                form.setId(player.wallNpc.get(force.id).getWallHeroLvId());
            }
            form.setCount(force.hp);
            form.setLine(force.maxLine);
            form.setCamp(force.getCamp());
            form.setHeroType(force.roleType);
            form.setCurLine(force.curLine);
            form.setIntensifyLv(force.getIntensifyLv() == 0 ? 1 : force.getIntensifyLv());
            if (player != null) {
                form.setOwnerId(player.lord.getNick());
            }
            recordData.addFormB(form);
        }
    }

    /**
     * 检测该玩家是否已经对该位置发起了战斗
     * 
     * @param pos
     * @param player
     * @return
     */
    public boolean checkPlayerSponsorBatter(int pos, Player player) {
        LinkedList<Battle> battleList = getBattlePosMap().get(pos);
        if (!CheckNull.isEmpty(battleList)) {
            for (Battle battle : battleList) {
                if (battle.getSponsor() != null && battle.getSponsor().roleId == player.roleId) {
                    return true;
                }
            }
        }
        return false;
    }

    public Map<Long, Integer> getRebelBattleCacheMap() {
        return rebelBattleCacheMap;
    }

}