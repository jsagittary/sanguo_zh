package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * User:        zhoujie
 * Date:        2020/2/14 16:40
 * Description:
 */
public class StaticActRobinHood {

    /**
     * 主键
     */
    private int id;
    /**
     * 对应activity_plan表中的活动id
     */
    private int actvityId;
    /**
     * 任务的id
     */
    private int taskId;
    /**
     * 任务条件类型
     */
    private int cond;
    /**
     * 任务条件ID
     */
    private int condId;
    /**
     * 任务完成次数
     */
    private int schedule;
    /**
     * 任务奖励
     */
    private List<List<Integer>> award;
    /**
     * 额外参数
     */
    private List<Integer> param;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getActvityId() {
        return actvityId;
    }

    public void setActvityId(int actvityId) {
        this.actvityId = actvityId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getCond() {
        return cond;
    }

    public void setCond(int cond) {
        this.cond = cond;
    }

    public int getCondId() {
        return condId;
    }

    public void setCondId(int condId) {
        this.condId = condId;
    }

    public int getSchedule() {
        return schedule;
    }

    public void setSchedule(int schedule) {
        this.schedule = schedule;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public List<Integer> getParam() {
        return param;
    }

    public void setParam(List<Integer> param) {
        this.param = param;
    }
}
