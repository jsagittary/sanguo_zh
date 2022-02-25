package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

public class StaticChristmasAward {
    private int id;
    private List<Integer> lv;
    private List<List<Integer>> awardList;
    private int activityId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getLv() {
        return lv;
    }

    public void setLv(List<Integer> lv) {
        this.lv = lv;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }
}
