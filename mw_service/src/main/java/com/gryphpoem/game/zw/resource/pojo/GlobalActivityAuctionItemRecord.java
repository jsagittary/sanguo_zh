package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.CommonPb;

import java.util.Date;

/**
 * 秋季拍卖 拍卖物品竞拍记录
 */
public class GlobalActivityAuctionItemRecord {
    /**
     * 玩家ID
     */
    private long lordId;
    /**
     * 玩家昵称
     */
    private String nickName;
    /**
     * 竞拍价格
     */
    private int costPrice;
    /**
     * 竞拍时间
     */
    private int biddingTime;

    /**
     * 是否一口价
     */
    private boolean makeDeal;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(int costPrice) {
        this.costPrice = costPrice;
    }

    public int getBiddingTime() {
        return biddingTime;
    }

    public void setBiddingTime(int biddingTime) {
        this.biddingTime = biddingTime;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public boolean isMakeDeal() {
        return makeDeal;
    }

    public void setMakeDeal(boolean makeDeal) {
        this.makeDeal = makeDeal;
    }

    /**
     * 反序列化
     * @param record
     * @return
     */
    public static GlobalActivityAuctionItemRecord deserialization(CommonPb.GlobalActivityAuctionItemRecord record) {
        GlobalActivityAuctionItemRecord result = new GlobalActivityAuctionItemRecord();
        result.setLordId(record.getLordId());
        result.setBiddingTime(record.getBiddingTime());
        result.setCostPrice(record.getCostPrice());
        result.setNickName(record.getNickName());

        return result;
    }

    public CommonPb.GlobalActivityAuctionItemRecord serialization(int itemId) {
        CommonPb.GlobalActivityAuctionItemRecord.Builder builder = CommonPb.GlobalActivityAuctionItemRecord.newBuilder();
        builder.setLordId(this.lordId);
        builder.setBiddingTime(biddingTime);
        builder.setNickName(nickName);
        builder.setCostPrice(costPrice);
        builder.setMakeDeal(makeDeal);
        builder.setItemId(itemId);

        return builder.build();
    }
}
