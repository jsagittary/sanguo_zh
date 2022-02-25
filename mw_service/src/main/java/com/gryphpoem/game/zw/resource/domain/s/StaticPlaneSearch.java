package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-12 11:31
 * @description: 战机抽取卡
 * @modified By:
 */
public class StaticPlaneSearch {

    private int Id;                     // 自增id
    private int type;                   // 1低级卡池  2高级卡池
    private int rewardType;             // 奖励结果类型，1 战机，2 战机碎片或物品
    private List<List<Integer>> reward; // 具体奖励，格式：[[type,id,count]...]
    private int weight;                 // 随机到的权重

    public int getRewardType() {
        return rewardType;
    }

    public void setRewardType(int rewardType) {
        this.rewardType = rewardType;
    }

    public List<List<Integer>> getReward() {
        return reward;
    }

    public void setReward(List<List<Integer>> reward) {
        this.reward = reward;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
