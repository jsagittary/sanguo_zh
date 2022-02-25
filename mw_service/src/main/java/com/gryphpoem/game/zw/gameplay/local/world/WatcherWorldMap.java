package com.gryphpoem.game.zw.gameplay.local.world;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.gryphpoem.game.zw.core.util.LogUtil;

/**
 * @ClassName WatcherWorldMap.java
 * @Description 用于管理观察者
 * @author QiuKun
 * @date 2019年3月22日
 */
public class WatcherWorldMap extends BaseCalcWroldMap {

    /** 玩家的关心的块数据 <roleId,set<cellId>> */
    private final Map<Long, Set<Integer>> playerCareCellId;
    /** 块中包含的玩家 <cellId,set<roleId>> */
    private final Map<Integer, Set<Long>> cellIdContainPlayer;

    public WatcherWorldMap(int mapId, int width, int height, int cellSize) {
        super(mapId, width, height, cellSize);
        playerCareCellId = new ConcurrentHashMap<>();
        cellIdContainPlayer = new ConcurrentHashMap<>(100);
    }

    public void rmWatcher(long roleId) {
        LogUtil.common("玩家退出登录 移除观察者 roleId:", roleId);
        removeWatcher(roleId);
    }

    /**
     * 进入地图
     * 
     * @param roleId
     */
    public void enterMap(long roleId) {
        playerCareCellId.computeIfAbsent(roleId, (k) -> new HashSet<>());
    }

    /**
     * 添加观察者
     * 
     * @param roleId
     * @param cellId
     */
    public void addWatcher(long roleId, int cellId) {
        if (!playerCareCellId.containsKey(roleId)) {
            return;// 没有调用进入世界协议 ,就不让添加观察
        }
        if (!checkCellIsValid(cellId)) {
            return;
        }
        Set<Integer> cellSet = playerCareCellId.computeIfAbsent(roleId, (k) -> new HashSet<>());
        cellSet.add(cellId);
        Set<Long> roleSet = cellIdContainPlayer.computeIfAbsent(cellId, (k) -> new HashSet<>());
        roleSet.add(roleId);
    }

    /**
     * 移除观察者不关心的cellId
     * 
     * @param roleId
     * @param cellId
     */
    public void removeWatcherCellId(long roleId, int cellId) {
        if (!checkCellIsValid(cellId)) {
            return;
        }
        Set<Integer> cellSet = playerCareCellId.get(roleId);
        if (cellSet != null && !cellSet.isEmpty()) {
            cellSet.remove(cellId);
            Set<Long> playerSet = cellIdContainPlayer.get(cellId);
            if (playerSet != null && !playerSet.isEmpty()) {
                playerSet.remove(roleId);
            }
        }
    }

    /**
     * 移除观察者
     * 
     * @param roleId
     */
    public void removeWatcher(long roleId) {
        Set<Integer> cellSet = playerCareCellId.remove(roleId);
        if (cellSet != null && !cellSet.isEmpty()) {
            for (Integer cellId : cellSet) {
                Set<Long> playerSet = cellIdContainPlayer.get(cellId);
                if (playerSet != null && !playerSet.isEmpty()) {
                    playerSet.remove(roleId);
                }
            }
        }
    }

    public Map<Long, Set<Integer>> getPlayerCareCellId() {
        return playerCareCellId;
    }

    public Map<Integer, Set<Long>> getCellIdContainPlayer() {
        return cellIdContainPlayer;
    }

//    public int randomPosByCity(int cityPos) {
//        int[] xy = posToXy(cityPos);
//        int x = xy[0];
//        int y = xy[1];
//        int randomX = RandomHelper.randomInArea(x - 11, x + 8);
//        int randomY = RandomHelper.randomInArea(y - 9, y + 10);
//        return xyToPos(randomX, randomY);
//    }
}
