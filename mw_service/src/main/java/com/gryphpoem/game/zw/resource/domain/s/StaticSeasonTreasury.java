package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author xwind
 * @date 2021/4/19
 */
public class StaticSeasonTreasury {
    private int taskId;
    private int taskType;
    private List<Integer> taskCond;
    private int category;
    private int serial;
    private List<List<Integer>> rdmAward;
    private int season;

    private int weight;

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public int getSerial() {
        return serial;
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }

    public List<Integer> getTaskCond() {
        return taskCond;
    }

    public void setTaskCond(List<Integer> taskCond) {
        this.taskCond = taskCond;
    }

    public List<List<Integer>> getRdmAward() {
        return rdmAward;
    }

    public void setRdmAward(List<List<Integer>> rdmAward) {
        this.rdmAward = rdmAward;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
