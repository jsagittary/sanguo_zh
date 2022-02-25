package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @program: server
 * @description:
 * @author: zhou jie
 * @create: 2019-12-02 14:44
 */
public class StaticBattlePassTask {

    /**
     * 任务id
     */
    private int id;
    /**
     * 任务类型：
     * 1 每日任务
     * 2 每周任务
     * 3 每月任务
     */
    private int type;
    /**
     * 任务条件类型
     */
    private int cond;
    /**
     * 任务条件Id
     */
    private int condId;
    /**
     * 完成任务需要达成的条件
     */
    private int schedule;
    /**
     * 完成任务后获取的战令经验
     */
    private int award;

    /**
     * plan表里的Key
     */
    private int planKey;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    public int getAward() {
        return award;
    }

    public void setAward(int award) {
        this.award = award;
    }

    public int getPlanKey() {
        return planKey;
    }

    public void setPlanKey(int planKey) {
        this.planKey = planKey;
    }
}