package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * Created by pengshuo on 2019/4/13 10:14
 * <br>Description: 改*巴顿活动
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class StaticActBarton {
    /** 商店格子，总共6格 */
    private int keyId;
    /** 商店格子，总共6格 */
    private int cond;
    /** 商店出售道具[[类型,ID,数量,权重],……]]，权重用于刷新几率 */
    private List<List<Integer>> awardList;
    /** 初始价格 */
    private int price;
    /** 活动档位 */
    private int activityId;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getCond() {
        return cond;
    }

    public void setCond(int cond) {
        this.cond = cond;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    @Override
    public String toString() {
        return "StaticActBarton{" +
                "keyId=" + keyId +
                ", cond=" + cond +
                ", awardList=" + awardList +
                ", price=" + price +
                ", activityId=" + activityId +
                '}';
    }
}
