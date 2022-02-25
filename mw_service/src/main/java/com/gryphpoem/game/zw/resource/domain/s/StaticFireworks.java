package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author xwind
 * @date 2021/12/20
 */
public class StaticFireworks {
    private int id;
    private int activityId;
    private List<List<Integer>> cost;

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

    public List<List<Integer>> getCost() {
        return cost;
    }

    public void setCost(List<List<Integer>> cost) {
        this.cost = cost;
    }
}
