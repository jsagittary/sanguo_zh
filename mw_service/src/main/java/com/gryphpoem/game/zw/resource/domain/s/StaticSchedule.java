package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author QiuKun
 * @ClassName StaticSchedule.java
 * @Description 世界进度  (s_schedule)
 * @date 2019年2月21日
 */
public class StaticSchedule {

    /**
     * 进度id
     */
    private int id;

    /**
     * 持续时间 单位天, -1表示按照boss死亡时间
     */
    private int durationTime;

    /**
     * 限时目标,对应限时目标id 格式[goalId,goalId]
     */
    private List<Integer> goal;

    /**
     * 排行榜类型: 1.阵营补给排行榜, 2.积分排行榜, 3.伤害排行榜
     */
    private int rankType;

    /**
     * 积分排行榜参数,对应city表的cityPointType
     */
    private int rankParam;

    /**
     * 排行榜是否区分区域 0 不区分, 1 区分
     */
    private int rankArea;

    /**
     * boss的阵型 格式[npcId,npcId]
     */
    private List<Integer> bossForm;

    /**
     * 可攻打城池type 格式: [cityType]
     */
    private List<Integer> attckCity;

    /**
     * 完成所有目标后 能否提前结束 0-不能 1-可以
     */
    private int endAhead;

    /**
     * BOSS概率掉落 [物品类型，权重]
     */
    private List<List<Integer>> bossDrop;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(int durationTime) {
        this.durationTime = durationTime;
    }

    public List<Integer> getGoal() {
        return goal;
    }

    public void setGoal(List<Integer> goal) {
        this.goal = goal;
    }

    public int getRankType() {
        return rankType;
    }

    public void setRankType(int rankType) {
        this.rankType = rankType;
    }

    public int getRankParam() {
        return rankParam;
    }

    public void setRankParam(int rankParam) {
        this.rankParam = rankParam;
    }

    public int getRankArea() {
        return rankArea;
    }

    public void setRankArea(int rankArea) {
        this.rankArea = rankArea;
    }

    public List<Integer> getAttckCity() {
        return attckCity;
    }

    public void setAttckCity(List<Integer> attckCity) {
        this.attckCity = attckCity;
    }

    public List<Integer> getBossForm() {
        return bossForm;
    }

    public void setBossForm(List<Integer> bossForm) {
        this.bossForm = bossForm;
    }

    public List<List<Integer>> getBossDrop() {
        return bossDrop;
    }

    public void setBossDrop(List<List<Integer>> bossDrop) {
        this.bossDrop = bossDrop;
    }

    /**
     * 是否可以提前结束
     * @return
     */
    public boolean canEndAhead() {
        return endAhead == 1;
    }
}
