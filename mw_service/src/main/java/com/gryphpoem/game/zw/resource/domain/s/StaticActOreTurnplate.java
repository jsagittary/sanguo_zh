package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 
* @ClassName: StaticActOreTurnplate
* @Description: 矿石转盘
* @author chenqi
* @date 2018年8月18日
*
 */
public class StaticActOreTurnplate {
    private int id; // 唯一id
    private int activityId;
    private int type; // actType
    private List<Integer> award; // 奖励
    private int weight;
    private int betterAward;// 1-标记为较好的物品 0-普通物品

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
