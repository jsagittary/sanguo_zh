package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 经济订单
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/11 11:38
 */
public class StaticEconomicOrder {

    private int id;

    /**
     * 需要的领主等级
     */
    private List<Integer> needLordLv;
    /**
     * 订单品质：1-普通; 2-稀有; 3-传说
     */
    private int quantity;

    /**
     * 订单出处, area id和集市建筑id, [集市，酒泉郡城，武陵郡城，南阳郡城，会稽郡城，丹阳郡城，阳平郡城，常山郡城，敦煌郡城，武威，江夏，交趾，巨鹿，洛阳]
     */
    private List<Integer> place;

    /**
     * 订单需求1  [[作物档位1, 作物档位1],[数量下限，数量上限]]
     */
    private List<List<Integer>> orderDemand1;

    /**
     * 订单需求2  [[作物档位1, 作物档位1],[数量下限，数量上限]]
     */
    private List<List<Integer>> orderDemand2;

    /**
     * 订单需求3  [[作物档位1, 作物档位1],[数量下限，数量上限]]
     */
    private List<List<Integer>> orderDemand3;

    /**
     * 订单奖励1  [[rewardType, subType], [rewardType, subType], [rewardType, subType]]
     */
    private List<List<Integer>> reward1;

    /**
     * 订单奖励数量1[下限数量，上限数量]
     */
    private List<Integer> number1;

    /**
     * 订单奖励2  [[rewardType, subType], [rewardType, subType], [rewardType, subType]]
     */
    private List<List<Integer>> reward2;

    /**
     * 订单奖励数量2[下限数量，上限数量]
     */
    private List<Integer> number2;

    /**
     * 特殊奖励[[type,id,count],[],[],[]]
     */
    private List<List<Integer>> specialReward;


    /**
     * 订单权重，在相同等级区间下，订单权重和为10000
     */
    private int weight;

    /**
     * 订单的预显示时间, 秒
     */
    private int preDisplayTime;

    /**
     * 订单的有效时间, 秒
     */
    private int durationTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getNeedLordLv() {
        return needLordLv;
    }

    public void setNeedLordLv(List<Integer> needLordLv) {
        this.needLordLv = needLordLv;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public List<Integer> getPlace() {
        return place;
    }

    public void setPlace(List<Integer> place) {
        this.place = place;
    }

    public List<List<Integer>> getOrderDemand1() {
        return orderDemand1;
    }

    public void setOrderDemand1(List<List<Integer>> orderDemand1) {
        this.orderDemand1 = orderDemand1;
    }

    public List<List<Integer>> getOrderDemand2() {
        return orderDemand2;
    }

    public void setOrderDemand2(List<List<Integer>> orderDemand2) {
        this.orderDemand2 = orderDemand2;
    }

    public List<List<Integer>> getOrderDemand3() {
        return orderDemand3;
    }

    public void setOrderDemand3(List<List<Integer>> orderDemand3) {
        this.orderDemand3 = orderDemand3;
    }

    public List<List<Integer>> getReward1() {
        return reward1;
    }

    public void setReward1(List<List<Integer>> reward1) {
        this.reward1 = reward1;
    }

    public List<Integer> getNumber1() {
        return number1;
    }

    public void setNumber1(List<Integer> number1) {
        this.number1 = number1;
    }

    public List<List<Integer>> getReward2() {
        return reward2;
    }

    public void setReward2(List<List<Integer>> reward2) {
        this.reward2 = reward2;
    }

    public List<Integer> getNumber2() {
        return number2;
    }

    public void setNumber2(List<Integer> number2) {
        this.number2 = number2;
    }

    public List<List<Integer>> getSpecialReward() {
        return specialReward;
    }

    public void setSpecialReward(List<List<Integer>> specialReward) {
        this.specialReward = specialReward;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getPreDisplayTime() {
        return preDisplayTime;
    }

    public void setPreDisplayTime(int preDisplayTime) {
        this.preDisplayTime = preDisplayTime;
    }

    public int getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(int durationTime) {
        this.durationTime = durationTime;
    }

}
