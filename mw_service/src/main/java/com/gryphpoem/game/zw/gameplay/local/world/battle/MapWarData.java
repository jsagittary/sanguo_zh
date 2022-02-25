package com.gryphpoem.game.zw.gameplay.local.world.battle;

import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
import com.gryphpoem.game.zw.gameplay.local.util.DelayQueue;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @ClassName MapWarData.java
 * @Description 地图上战斗相关
 * @author QiuKun
 * @date 2019年3月22日
 */
public class MapWarData implements DelayInvokeEnvironment {
    /** 地图信息 */
    private final CrossWorldMap crossWorldMap;

    /** 地图上所有战斗 <battleId,BaseMapBattle> */
    private final Map<Integer, BaseMapBattle> allBattles;
    /** 地图上战斗 battle的缓存 <pos,list<battleId>> */
    private final Map<Integer, List<Integer>> battlePosCache;
    /** 部队的延迟队列 */
    private final DelayQueue<BaseMapBattle> delayBattleQueue;

    public MapWarData(CrossWorldMap crossWorldMap) {
        this.crossWorldMap = crossWorldMap;
        this.allBattles = new ConcurrentHashMap<>();
        this.battlePosCache = new ConcurrentHashMap<>();
        this.delayBattleQueue = new DelayQueue<>(this);
    }

    /**
     * 添加战斗
     * 
     * @param baseBattle
     * @return
     */
    public boolean addBattle(BaseMapBattle baseBattle) {
        if (baseBattle == null) return false;
        int battleId = baseBattle.getBattleId();
        allBattles.put(battleId, baseBattle);

        int pos = baseBattle.getBattle().getPos();
        List<Integer> battleList = battlePosCache.computeIfAbsent(pos, ArrayList::new);
        battleList.add(battleId);

        delayBattleQueue.add(baseBattle);
        return true;
    }

    /**
     * 取消某个点的战斗
     * 
     * @param pos
     * @return 该点的所有战斗
     */
    public List<Integer> cancelBattleByPos(int pos) {
        List<Integer> battleList = battlePosCache.remove(pos);
        if (battleList == null || battleList.isEmpty()) return null;
        for (Integer battleId : battleList) {
            BaseMapBattle battle = allBattles.remove(battleId);
            if (battle != null) delayBattleQueue.remove(battle);
        }
        return battleList;
    }

    /**
     * 取消某个点的战斗,并返回部队,和推送
     *
     * @param pos
     */
    public void cancelBattleByPosAndReturnArmy(int pos, BaseMapBattle.CancelBattleType cancelType) {
        List<Integer> battleIds = battlePosCache.get(pos);
        if (battleIds != null) {
            List<MapEvent> allMapEvents = new ArrayList<>();
            for (Integer battleId : battleIds) {
                BaseMapBattle baseMapBattle = getAllBattles().get(battleId);
                if (baseMapBattle != null) {
                    List<MapEvent> mapEvents = BaseMapBattle.returnArmyBattle(this, baseMapBattle, cancelType);
                    allMapEvents.addAll(mapEvents);
                    baseMapBattle.onCancelBattleAfter(this, cancelType);
                }
            }
            allMapEvents.add(MapEvent.mapEntity(pos, MapCurdEvent.UPDATE));
            crossWorldMap.publishMapEvent(allMapEvents);
            this.cancelBattleByPos(pos);
        }
    }

    /**
     * 取消某个id的战斗
     * 
     * @param battleId
     */
    public void cancelBattleById(int battleId) {
        BaseMapBattle b = allBattles.remove(battleId);
        if (b != null) {
            int pos = b.getBattle().getPos();
            List<Integer> battleIdList = battlePosCache.get(pos);
            if (battleIdList != null) {
                battleIdList.remove(Integer.valueOf(battleId));
                if (battleIdList.isEmpty()) {
                    battlePosCache.remove(pos);
                }
            }
            delayBattleQueue.remove(b);
        }
    }

    /**
     * 正常从队列中结束战斗
     * 
     * @param battleId
     */
    public void finishlBattle(int battleId) {
        BaseMapBattle b = allBattles.remove(battleId);
        if (b != null) {
            int pos = b.getBattle().getPos();
            List<Integer> battleIdList = battlePosCache.get(pos);
            if (battleIdList != null) {
                battleIdList.remove(Integer.valueOf(battleId));
                if (battleIdList.isEmpty()) {
                    battlePosCache.remove(pos);
                }
            }
        }
    }

    /**
     * 获取坐标点上的所有战斗对象
     * 
     * @param pos 坐标
     * @return 战斗对象
     */
    public List<BaseMapBattle> getBattlesByPos(int pos) {
        List<Integer> battleIds = battlePosCache.get(pos);
        if (CheckNull.isEmpty(battleIds)) {
            return null;
        }
        return battleIds.stream().map(battleId -> allBattles.get(battleId)).collect(Collectors.toList());
    }

    public CrossWorldMap getCrossWorldMap() {
        return crossWorldMap;
    }

    public Map<Integer, BaseMapBattle> getAllBattles() {
        return allBattles;
    }

    public Map<Integer, List<Integer>> getBattlePosCache() {
        return battlePosCache;
    }

    public DelayQueue<BaseMapBattle> getDelayBattleQueue() {
        return delayBattleQueue;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public DelayQueue getDelayQueue() {
        return getDelayBattleQueue();
    }

}
