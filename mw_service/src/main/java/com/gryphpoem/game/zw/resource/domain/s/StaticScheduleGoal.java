package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2019-02-25 16:25
 * @description: 世界进度目标 (s_schedule_goal)
 * @modified By:
 */
public class StaticScheduleGoal {

    /**
     * s_schedule_goal表中的主键
     */
    private int id;

    /**
     * 对应 s_schedule表的id
     */
    private int scheduleId;

    /**
     * 条件类型 1.指挥官基地提升至N级 2.N个城被攻克 3.部队最大战力达到 4.对防线攻击N次5.攻打盖世太保或流寇
     */
    private int cond;

    /**
     * 条件类型id,没有填0
     */
    private int condId;

    /**
     * 需要达成的条件
     */
    private int schedule;

    /**
     * 奖励 格式[[type,id,cnt]]'
     */
    private List<List<Integer>> award;

    /**
     * 进度结束未完成目标是否可以领取奖励, 0.不可以; 1.可以
     */
    private int finishCanGain;

    /**
     * 进度结束后是否可以继续增加进度 ,0 不可以;1可以
     */
    private int finishUpdate;

    /**
     * 进度结束未完成目标是否可以领取奖励
     * @return
     */
    public boolean canFinishGain() {
        return finishCanGain == 1;
    }

    /**
     * 进度结束后是否可以继续增加进度
     * @return
     */
    public boolean canFinishUpdate() {
        return finishUpdate == 1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
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

    public int getFinishCanGain() {
        return finishCanGain;
    }

    public void setFinishCanGain(int finishCanGain) {
        this.finishCanGain = finishCanGain;
    }

    public int getFinishUpdate() {
        return finishUpdate;
    }

    public void setFinishUpdate(int finishUpdate) {
        this.finishUpdate = finishUpdate;
    }
}
