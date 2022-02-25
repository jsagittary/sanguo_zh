package com.gryphpoem.game.zw.resource.domain.s;

/**
 * 皮肤商店
 * @Description
 * @Author zhangdh
 * @Date 2021-07-27 11:27
 */
public class StaticActSkinEncore {
    private int id;
    private int skinId;
    private int payId;
    private int activityId;
    private int goldCost;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSkinId() {
        return skinId;
    }

    public void setSkinId(int skinId) {
        this.skinId = skinId;
    }

    public int getPayId() {
        return payId;
    }

    public void setPayId(int payId) {
        this.payId = payId;
    }

    public int getGoldCost() {
        return goldCost;
    }

    public void setGoldCost(int goldCost) {
        this.goldCost = goldCost;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }
}
