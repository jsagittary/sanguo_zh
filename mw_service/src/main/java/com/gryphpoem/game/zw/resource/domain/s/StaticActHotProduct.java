package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 热销商品
 * @program: empire_en
 * @description:
 * @author: zhou jie
 * @create: 2020-08-25 15:51
 */
public class StaticActHotProduct {

    /**
     * 主键
     */
    private int keyId;
    /**
     * 活动id, 档位
     */
    private int activityId;
    /**
     * 活动类型
     */
    private int type;
    /**
     * 页签
     * 1: 购买礼包
     * 2: 消耗奖励
     */
    private int tab;
    /**
     * 原价
     */
    private int price;
    /**
     * 限购次数, 当tab为1的时候
     */
    private int time;
    /**
     * 当tab为2的时候, 消耗奖励中消耗钻石数量档位
     */
    private int spend;
    /**
     * 奖励内容
     */
    private List<List<Integer>> awardList;
    /**
     * 折扣变量 [初始折扣, 每次购买后的折扣]
     */
    private List<Integer> discount;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
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

    public int getTab() {
        return tab;
    }

    public void setTab(int tab) {
        this.tab = tab;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getSpend() {
        return spend;
    }

    public void setSpend(int spend) {
        this.spend = spend;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public List<Integer> getDiscount() {
        return discount;
    }

    public void setDiscount(List<Integer> discount) {
        this.discount = discount;
    }
}