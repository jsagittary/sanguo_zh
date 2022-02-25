package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 玛雅音乐节创作室
 * @Description
 * @Author zhangdh
 * @Date 2021-10-27 14:00
 */
public class StaticCreativeOffice {
    private int id;
    private int activityId;
    private int type;
    private int taskId;
    private List<Integer> params;
    private List<List<Integer>> awardList;

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

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
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
}
