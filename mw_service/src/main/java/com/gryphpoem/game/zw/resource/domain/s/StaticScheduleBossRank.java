package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @program: zombie_trunk
 * @description: 对应s_schedule_boss_rank表
 * @author: zhou jie
 * @create: 2019-10-15 16:12
 */
public class StaticScheduleBossRank {

    private int id;
    /**
     * 世界进程id
     */
    private int scheduleId;
    /**
     * 排名区间
     */
    private List<Integer> rank;
    /**
     * 奖励
     */
    private List<List<Integer>> award;

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

    public List<Integer> getRank() {
        return rank;
    }

    public void setRank(List<Integer> rank) {
        this.rank = rank;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }
}