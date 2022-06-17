package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

public class StaticActTreasureWareJourney {
    private int keyId;

    private int activityId;

    private int taskType;

    private List<Integer> params;

    private List<List<Integer>> awardList;

    private boolean fromNow;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
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

    public List<Integer> getParams() {
        return params;
    }

    public void setParams(List<Integer> params) {
        this.params = params;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public boolean isFromNow() {
        return fromNow;
    }

    public void setFromNow(boolean fromNow) {
        this.fromNow = fromNow;
    }
}
