package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

public class StaticWorldTask {
    private int taskId;// 任务id
    private int triggerId;//
    private int taskType;// 1匪军通缉令,2攻克营城,3世界boss
    private List<Integer> param;// 条件(boosId,cityId 等)
    private int cond;// 次数，数量
    private List<List<Integer>> awardList;//
    private List<List<Integer>> campAward;//军团双倍
    private int hp;

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(int triggerId) {
        this.triggerId = triggerId;
    }

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public List<Integer> getParam() {
        return param;
    }

    public void setParam(List<Integer> param) {
        this.param = param;
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

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public List<List<Integer>> getCampAward() {
        return campAward;
    }

    public void setCampAward(List<List<Integer>> campAward) {
        this.campAward = campAward;
    }

}
