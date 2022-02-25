package com.gryphpoem.game.zw.resource.domain.s;

/**
 * User:        zhoujie
 * Date:        2020/4/2 17:03
 * Description:
 */
public class StaticRoyalArenaTask {

    private int id;
    /**
     * 活动id
     */
    private int activityId;
    /**
     * 任务类型
     */
    private int taskType;
    /**
     * 星级
     */
    private int star;
    /**
     * random星级的概率
     */
    private int randomStr;
    /**
     * 参数
     */
    private int param;
    /**
     * 条件
     */
    private int cond;

    /**
     * 完成后加的贡献值
     */
    private int pts;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public int getRandomStr() {
        return randomStr;
    }

    public void setRandomStr(int randomStr) {
        this.randomStr = randomStr;
    }

    public int getParam() {
        return param;
    }

    public void setParam(int param) {
        this.param = param;
    }

    public int getCond() {
        return cond;
    }

    public void setCond(int cond) {
        this.cond = cond;
    }

    public int getPts() {
        return pts;
    }

    public void setPts(int pts) {
        this.pts = pts;
    }
}
