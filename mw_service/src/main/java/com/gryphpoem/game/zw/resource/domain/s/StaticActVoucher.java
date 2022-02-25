package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 代币转换表
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2020-09-10 12:07
 */
public class StaticActVoucher {

    /**
     * 唯一值id
     */
    private int id;
    /**
     * 活动type
     */
    private int type;
    /**
     * 活动id
     */
    private int activityId;
    /**
     * 代币道具
     */
    private List<Integer> consume;
    /**
     * 活动过期后代币转换的道具
     */
    private List<Integer> awardList;
    /**
     * 道具不足时购买单次消耗价格
     */
    private int price;

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

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public List<Integer> getConsume() {
        return consume;
    }

    public void setConsume(List<Integer> consume) {
        this.consume = consume;
    }

    public List<Integer> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<Integer> awardList) {
        this.awardList = awardList;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}