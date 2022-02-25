package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticActPayTurnplate.java
 * @Description 充值转盘
 * @author QiuKun
 * @date 2018年6月21日
 */
public class StaticActPayTurnplate {
    private int id; // 唯一id
    private int activityId;
    private int type; // actType
    private List<Integer> award; // 奖励
    private int weight;
    private int betterAward;// 1-标记为较好的物品 0-普通物品
    private int downFrequency; // 保底次数，≥此次数才可能抽到

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

    public List<Integer> getAward() {
        return award;
    }

    public void setAward(List<Integer> award) {
        this.award = award;
    }

    public int getDownFrequency() {
        return downFrequency;
    }

    public void setDownFrequency(int downFrequency) {
        this.downFrequency = downFrequency;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getBetterAward() {
        return betterAward;
    }

    public void setBetterAward(int betterAward) {
        this.betterAward = betterAward;
    }

}
