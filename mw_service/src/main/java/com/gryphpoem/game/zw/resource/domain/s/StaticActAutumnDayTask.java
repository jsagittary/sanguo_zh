package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 喜悦金秋任务表
 */
public class StaticActAutumnDayTask {

    /**
     * 任务序号
     */
    private int id;

    /**
     * 天数
     */
    private int day;

    /**
     * 活动类型
     */
    private int activityType;

    /**
     * 活动id
     */
    private int activityId;

    /**
     * 任务类型
     */
    private int taskid;

    /**
     * 任务参数
     */
    private List<Integer> param;

    /**
     * 完成任务后获得的积分、贡献值等特殊货币
     */
    private int integral;

    /**
     * 奖励列表
     */
    private List<List<Integer>> awardList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getActivityType() {
        return activityType;
    }

    public void setActivityType(int activityType) {
        this.activityType = activityType;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public List<Integer> getParam() {
        return param;
    }

    public void setParam(List<Integer> param) {
        this.param = param;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public int getTaskid() {
        return taskid;
    }

    public void setTaskid(int taskid) {
        this.taskid = taskid;
    }

    public int getIntegral() {
        return integral;
    }

    public void setIntegral(int integral) {
        this.integral = integral;
    }

    @Override
    public String toString() {
        return "StaticActAutumnDayTask{" +
                "id=" + id +
                ", day=" + day +
                ", activityType=" + activityType +
                ", activityId=" + activityId +
                ", taskid=" + taskid +
                ", param=" + param +
                ", integral=" + integral +
                ", awardList=" + awardList +
                '}';
    }
}
