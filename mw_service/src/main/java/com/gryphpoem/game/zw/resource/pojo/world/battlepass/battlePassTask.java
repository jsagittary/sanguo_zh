package com.gryphpoem.game.zw.resource.pojo.world.battlepass;

import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.domain.s.StaticBattlePassTask;

/**
 * @program: server
 * @description:
 * @author: zhou jie
 * @create: 2019-12-04 11:32
 */
public class battlePassTask {

    /**
     * 任务条件
     */
    private int taskId;
    /**
     * 任务进度
     */
    private int schedule;
    /**
     * 任务的领取状态
     */
    private int status;

    public battlePassTask() {
    }

    public battlePassTask(int taskId) {
        this.taskId = taskId;
        this.schedule = 0;
        this.status = TaskType.TYPE_STATUS_UNFINISH;
    }


    public battlePassTask(SerializePb.SerBattlePassTask serTask) {
        this.taskId = serTask.getTaskId();
        this.schedule = serTask.getSchedule();
        this.status = serTask.getStatus();
    }

    /**
     * 是否完成了
     *
     * @return true 已经领取或者已经达成条件
     */
    public boolean isFinished() {
        return status == TaskType.TYPE_STATUS_FINISH || status == TaskType.TYPE_STATUS_REWARD;
    }

    /**
     * 设置任务的进度
     *
     * @param schedule 进度
     * @param sTask    任务配置
     */
    public void setSchedule(int schedule, StaticBattlePassTask sTask) {
        this.schedule = schedule;
        if (sTask != null && status == 0 && schedule >= sTask.getSchedule()) {
            setStatus(TaskType.TYPE_STATUS_FINISH);
        }
    }


    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getSchedule() {
        return schedule;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * 序列化战令任务的进度
     * @return 序列化对象
     */
    public SerializePb.SerBattlePassTask ser() {
        SerializePb.SerBattlePassTask.Builder builder = SerializePb.SerBattlePassTask.newBuilder();
        builder.setTaskId(this.taskId);
        builder.setSchedule(this.schedule);
        builder.setStatus(this.status);
        return builder.build();
    }
}