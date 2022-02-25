package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.domain.s.StaticActRobinHood;

/**
 * User:        zhoujie
 * Date:        2020/2/14 16:32
 * Description:
 */
public class RobinHoodTask {

    /**
     * 任务奖励未领取
     */
    public static final int AWARD_STATUS_0 = 0;
    /**
     * 任务奖励可领取
     */
    public static final int AWARD_STATUS_1 = 1;
    /**
     * 任务奖励已领取
     */
    public static final int AWARD_STATUS_2 = 2;
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

    public RobinHoodTask() {
    }

    /**
     * 初始化构造
     * @param arh
     */
    public RobinHoodTask(StaticActRobinHood arh) {
        this.taskId = arh.getTaskId();
        this.schedule = 0;
        this.status = RobinHoodTask.AWARD_STATUS_0;
    }

    /**
     * 反序列化
     * @param robinHoodTask
     */
    public RobinHoodTask(CommonPb.RobinHoodTask robinHoodTask) {
        this.taskId = robinHoodTask.getKeyId();
        this.schedule = robinHoodTask.getSchedule();
        this.status = robinHoodTask.getStatus();
    }


    /**
     * 序列化
     * @return
     */
    public CommonPb.RobinHoodTask ser() {
        CommonPb.RobinHoodTask.Builder builder = CommonPb.RobinHoodTask.newBuilder();
        builder.setKeyId(this.taskId);
        builder.setSchedule(this.schedule);
        builder.setStatus(this.status);
        return builder.build();
    }

    /**
     * 设置任务的进度
     * @param schedule
     * @param sConf
     */
    public void setSchedule(int schedule, StaticActRobinHood sConf) {
        this.schedule = schedule;
        if (sConf != null && status == RobinHoodTask.AWARD_STATUS_0 && schedule >= sConf.getSchedule()) {
            setStatus(RobinHoodTask.AWARD_STATUS_1);
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

}
