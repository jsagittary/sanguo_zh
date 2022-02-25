package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 活动奖励
 * 
 * @author tyler
 *
 */
public class StaticActAward {

    private int keyId;
    private int activityId;
    private int sortId;
    private int cond;
    private List<List<Integer>> awardList;
    private String desc;
    private List<Integer> param;// 是排行榜活动时: 0位置表示档位,1位置表示开始名次
    private int type;
    private int taskType;

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

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

    public int getSortId() {
        return sortId;
    }

    public void setSortId(int sortId) {
        this.sortId = sortId;
    }

    public int getCond() {
        return cond;
    }

    public void setCond(int cond) {
        this.cond = cond;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<Integer> getParam() {
        return param;
    }

    public void setParam(List<Integer> param) {
        this.param = param;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "StaticActAward [keyId=" + keyId + ", activityId=" + activityId + ", sortId=" + sortId + ", cond=" + cond
                + ", awardList=" + awardList + ", desc=" + desc + ", param=" + param + "]";
    }
}
