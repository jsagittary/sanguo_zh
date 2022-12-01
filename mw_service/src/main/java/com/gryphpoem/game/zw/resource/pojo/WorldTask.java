package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.s.StaticWorldTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 世界任务
 *
 * @author tyler
 */
public class WorldTask {
    private AtomicInteger worldTaskId;// 当前任务ID
    private Map<Integer, CommonPb.WorldTask> worldTaskMap = new ConcurrentHashMap<>();// 任务完成状态
    private Fighter defender;// 世界boss血量

    public WorldTask(StaticWorldTask staticWorldTask) {
        worldTaskId = new AtomicInteger(WorldConstant.INIT_WORLD_TASKID);// 初始化任务
        worldTaskMap = new ConcurrentHashMap<>();// 任务完成状态
        worldTaskMap.put(WorldConstant.INIT_WORLD_TASKID, CommonPb.WorldTask.newBuilder()
                .setTaskId(WorldConstant.INIT_WORLD_TASKID).setHp(staticWorldTask.getHp()).build());
    }

    public AtomicInteger getWorldTaskId() {
        int curTid = getCurWorldTaskId();
        // 修正当前任务ID
        if (worldTaskId.get() != curTid) {
            worldTaskId.set(curTid);
        }
        return worldTaskId;
    }

    /**
     * 获取当前的任务Id
     *
     * @return
     */
    private int getCurWorldTaskId() {
        int curTaskId = WorldConstant.INIT_WORLD_TASKID;
        for (int i = WorldConstant.INIT_WORLD_TASKID; i <= StaticWorldDataMgr.getLastWorldTask().getTaskId(); i++) {
            CommonPb.WorldTask worldTask = worldTaskMap.get(i);
            if (worldTask == null) {// 当任务map中 2 3 5 任务,4 任务没有,说明当前任务是3
                return curTaskId;
            }
            curTaskId = i;
            StaticWorldTask swt = StaticWorldDataMgr.getWorldTask(i);
            if (swt.getTaskType() == TaskType.WORLD_TASK_TYPE_BOSS) {// boss任务
                if (worldTask.getHp() > 0) {
                    return curTaskId;
                }
            } else if (swt.getTaskType() == TaskType.WORLD_TASK_TYPE_CITY) {// 打城
                if (worldTask.getTaskCnt() < 1) {
                    return curTaskId;
                }
            }
        }
        return curTaskId;
    }

    public void setWorldTaskId(AtomicInteger worldTaskId) {
        this.worldTaskId = worldTaskId;
    }

    public Map<Integer, CommonPb.WorldTask> getWorldTaskMap() {
        return worldTaskMap;
    }

    public void setWorldTaskMap(Map<Integer, CommonPb.WorldTask> worldTaskMap) {
        this.worldTaskMap = worldTaskMap;
    }

    public Fighter getDefender() {
        return defender;
    }

    public void setDefender(Fighter defender) {
        this.defender = defender;
    }

    @Override
    public String toString() {
        return "WorldTask [worldTaskId=" + worldTaskId + ", worldTaskMap=" + worldTaskMap + ", defender=" + defender
                + "]";
    }

}
