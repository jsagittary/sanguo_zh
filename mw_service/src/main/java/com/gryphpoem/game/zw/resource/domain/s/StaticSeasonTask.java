package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author xwind
 * @date 2021/4/19
 */
public class StaticSeasonTask {
    private int id;
    private int taskId;
    private int taskType;
    private List<Integer> taskCond;
    private int preTaskId;
    private List<List<Integer>> taskAward;
    private int taskScore;
    private int season;

    public List<Integer> getTaskCond() {
        return taskCond;
    }

    public void setTaskCond(List<Integer> taskCond) {
        this.taskCond = taskCond;
    }

    public List<List<Integer>> getTaskAward() {
        return taskAward;
    }

    public void setTaskAward(List<List<Integer>> taskAward) {
        this.taskAward = taskAward;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public int getPreTaskId() {
        return preTaskId;
    }

    public void setPreTaskId(int preTaskId) {
        this.preTaskId = preTaskId;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public int getTaskScore() {
        return taskScore;
    }

    public void setTaskScore(int taskScore) {
        this.taskScore = taskScore;
    }
}
