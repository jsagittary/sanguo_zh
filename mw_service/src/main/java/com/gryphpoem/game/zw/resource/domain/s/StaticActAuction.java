package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.domain.p.ActivityAuctionConst;

import java.util.List;
import java.util.Objects;

public class StaticActAuction {
    /**
     * 拍卖道具id
     */
    private int id;
    /**
     * 活动id
     */
    private int activityId;
    /**
     * 轮数
     */
    private int round;
    /**
     * 拍卖品类型
     */
    private int type;
    /**
     * 拍卖品
     */
    private List<Integer> auctionItem;
    /**
     * 起拍价
     */
    private int startingPrice;
    /**
     * 最低加价
     */
    private int minMarkup;
    /**
     * 一口价
     */
    private int purchasePrice;

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

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(int startingPrice) {
        this.startingPrice = startingPrice;
    }

    public int getMinMarkup() {
        return minMarkup;
    }

    public void setMinMarkup(int minMarkup) {
        this.minMarkup = minMarkup;
    }

    public int getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(int purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public List<Integer> getAuctionItem() {
        return auctionItem;
    }

    public void setAuctionItem(List<Integer> auctionItem) {
        this.auctionItem = auctionItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaticActAuction that = (StaticActAuction) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * 是否可以参与竞价
     * @param cost
     * @param auctionConst
     * @param finalPrice
     * @return
     */
    public boolean canBidding(int cost, ActivityAuctionConst auctionConst, int finalPrice) {
        finalPrice = finalPrice == 0 ? startingPrice : finalPrice + minMarkup;
        if (auctionConst.equals(ActivityAuctionConst.ORDINARY_AUCTION)) {
            return cost >= finalPrice;
        }

        return true;
    }
}
