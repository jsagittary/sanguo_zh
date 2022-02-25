package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.domain.s.StaticRoyalArenaTask;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * User:        zhoujie
 * Date:        2020/4/2 14:10
 * Description:
 */
public class RoyalArenaTask {

    // 接受
    public static final int TASK_AGREE_0 = 0;
    // 放弃了
    public static final int TASK_AGREE_1 = 1;

    // 任务id
    private int taskId;

    // 星级
    private int star;

    // 是否接受了任务
    private int agree;

    // 任务状态
    private int status;

    // 任务进度
    private int schedule;

    // 任务刷新时间
    private int endTime;

    // 免费刷新次数
    private int freeRefreshCnt;


    /**
     * 转点清除数据
     */
    public void clearTaskAndData() {
        this.freeRefreshCnt = 0;
    }

    /**
     * 反序列化
     *
     * @param task
     */
    public RoyalArenaTask(CommonPb.RoyalArenaTask task) {
        this.taskId = task.getTaskId();
        this.star = task.getStar();
        this.agree = task.getAgree();
        this.status = task.getStatus();
        this.schedule = task.getSchedule();
        this.endTime = task.getEndTime();
        this.freeRefreshCnt = task.getFreeRefreshCnt();
    }

    /**
     * 自增刷新次数
     *
     * @return 增加后的次数
     */
    public int incrementFreeCnt() {
        return ++freeRefreshCnt;
    }

    /**
     * 刷新星级
     *
     * @param refreshTask
     */
    public void refreshStart(StaticRoyalArenaTask refreshTask) {
        if (!CheckNull.isNull(refreshTask)) {
            this.taskId = refreshTask.getId();
            this.star = refreshTask.getStar();
        }
    }

    public RoyalArenaTask(StaticRoyalArenaTask sTask) {
        this.taskId = sTask.getId();
        this.star = sTask.getStar();
        // 默认是未接受的
        this.agree = TASK_AGREE_0;
        this.status = TaskType.TYPE_STATUS_UNFINISH;
        this.schedule = 0;
        this.endTime = 0;
        this.freeRefreshCnt = 0;
    }

    /**
     * 设置进度
     *
     * @param schedule 进度
     * @param sTask    任务配置
     */
    public void setSchedule(int schedule, StaticRoyalArenaTask sTask) {
        this.schedule = schedule;
        if (sTask != null && status == TaskType.TYPE_STATUS_UNFINISH && schedule >= sTask.getCond()) {
            setStatus(TaskType.TYPE_STATUS_FINISH);
            // 任务完成后, 倒计时结束
            int now = TimeHelper.getCurrentSecond();
            setEndTime(now);
        }
    }

    /**
     * 序列化
     *
     * @return
     */
    public CommonPb.RoyalArenaTask ser() {
        CommonPb.RoyalArenaTask.Builder builder = CommonPb.RoyalArenaTask.newBuilder();
        builder.setTaskId(this.taskId);
        builder.setStar(this.star);
        builder.setEndTime(this.endTime);
        builder.setFreeRefreshCnt(this.freeRefreshCnt);
        builder.setSchedule(this.schedule);
        builder.setStatus(this.status);
        builder.setAgree(this.agree);
        return builder.build();
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public int getAgree() {
        return agree;
    }

    public void setAgree(int agree) {
        this.agree = agree;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSchedule() {
        return schedule;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getFreeRefreshCnt() {
        return freeRefreshCnt;
    }

    public void setFreeRefreshCnt(int freeRefreshCnt) {
        this.freeRefreshCnt = freeRefreshCnt;
    }
}
