package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * s_act_super_turnplate
 * @author xwind
 * @date 2021/7/22
 */
public class StaticAnniversaryTurntable {
    private int id;
    private int activityId;
    private int type;
    private int betterAward;
    private int weight;
    private List<List<Integer>> award;
    private int downFrequency;
    private int round;
    private int rank;

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

    public int getBetterAward() {
        return betterAward;
    }

    public void setBetterAward(int betterAward) {
        this.betterAward = betterAward;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public int getDownFrequency() {
        return downFrequency;
    }

    public void setDownFrequency(int downFrequency) {
        this.downFrequency = downFrequency;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
