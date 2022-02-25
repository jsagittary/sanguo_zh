package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.SerializePb;

public class ActivityAuctionRecord {
    /**
     * 竞拍的商品
     */
    private int itemId;

    /**
     * 自己消耗的钻石
     */
    private int costDiamond;

    /**
     * 当前玩家ID竞拍(如果是他人的id，则是被他人超越;自己的id则是当前玩家竞拍)
     */
    private long lordId;

    /**
     * 竞拍玩家的昵称,若为空则是自己
     */
    private String nickName;

    /**
     * 竞拍类型
     */
    private ActivityAuctionConst status;

    /**
     * 日志时间
     */
    private int logTime;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getCostDiamond() {
        return costDiamond;
    }

    public void setCostDiamond(int costDiamond) {
        this.costDiamond = costDiamond;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public ActivityAuctionConst getStatus() {
        return status;
    }

    public void setStatus(ActivityAuctionConst status) {
        this.status = status;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getLogTime() {
        return logTime;
    }

    public void setLogTime(int logTime) {
        this.logTime = logTime;
    }

    /**
     * 反序列化
     * @param activityAuctionRecordPb
     */
    public void deserialization(SerializePb.ActivityAuctionRecord activityAuctionRecordPb) {
        this.itemId = activityAuctionRecordPb.getItemId();
        this.costDiamond = activityAuctionRecordPb.getCostDiamond();
        this.lordId = activityAuctionRecordPb.getLordId();
        this.nickName = activityAuctionRecordPb.getNickName();
        this.status = ActivityAuctionConst.convertTo(activityAuctionRecordPb.getStatus());
        this.logTime = activityAuctionRecordPb.getLogTime();
    }

    /**
     * 序列化
     * @return
     */
    public SerializePb.ActivityAuctionRecord serialization() {
        SerializePb.ActivityAuctionRecord.Builder builder = SerializePb.ActivityAuctionRecord.newBuilder();
        builder.setNickName(nickName);
        builder.setStatus(status.getType());
        builder.setLordId(lordId);
        builder.setItemId(itemId);
        builder.setCostDiamond(costDiamond);
        builder.setLogTime(this.logTime);

        return builder.build();
    }
}
