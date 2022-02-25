package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * User:        zhoujie
 * Date:        2020/2/19 15:09
 * Description:
 */
public class StaticTurnplateExtra {

    /**
     * 主键
     */
    private int id;
    /**
     * actType
     */
    private int type;
    /**
     * 活动id
     */
    private int activityId;
    /**
     * 转盘次数
     */
    private int times;
    /**
     * 奖励
     */
    private List<List<Integer>> awardList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }
}
