package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-10-29 20:43
 */
public class StaticCreativeOfficeAward {
    private int id;
    private int activityId;
    private int type;
    private List<Integer> params;
    private List<List<Integer>> awards;

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<Integer> getParams() {
        return params;
    }

    public void setParams(List<Integer> params) {
        this.params = params;
    }

    public List<List<Integer>> getAwards() {
        return awards;
    }

    public void setAwards(List<List<Integer>> awards) {
        this.awards = awards;
    }
}
