package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.domain.p.ActivityAuctionConst;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.util.ObjectUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GlobalActivityAuctionItem {

    /**
     * 拍卖品id
     */
    private int itemId;
    /**
     * 拍卖品获得者ID(当前出价最高者)
     */
    private long ownerLordId;
    /**
     * 拍卖品获得者昵称(当前出价最高者)
     */
    private String nickName;
    /**
     * 拍卖品获得者阵营(当前出价最高者)
     */
    private int camp;
    /**
     * 拍卖品成交价(一口价)
     */
    private int finalPrice;
    /**
     * 竞拍记录
     */
    private List<GlobalActivityAuctionItemRecord> recordList;

    /**
     * 是否一口价出售
     */
    private boolean makeDeal;

    /**
     * 是否结算拍卖得主
     */
    private boolean settle;

    public GlobalActivityAuctionItem() {
    }

    public GlobalActivityAuctionItem(int itemId) {
        this.itemId = itemId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public long getOwnerLordId() {
        return ownerLordId;
    }

    public void setOwnerLordId(long ownerLordId) {
        this.ownerLordId = ownerLordId;
    }

    public int getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(int finalPrice) {
        this.finalPrice = finalPrice;
    }

    public List<GlobalActivityAuctionItemRecord> getRecordList() {
        if (CheckNull.isNull(this.recordList)) {
            this.recordList = new ArrayList<>();
        }

        return recordList;
    }

    public void setRecordList(List<GlobalActivityAuctionItemRecord> recordList) {
        this.recordList = recordList;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public boolean isMakeDeal() {
        return makeDeal;
    }

    public void setMakeDeal(boolean makeDeal) {
        this.makeDeal = makeDeal;
    }

    public boolean isSettle() {
        return settle;
    }

    public void setSettle(boolean settle) {
        this.settle = settle;
    }

    public void settle() {
        this.settle = true;
    }

    /**
     * 是否可以参与出价
     * @param cost
     * @param auctionConst
     * @return
     */
    public boolean canBidding(int cost, ActivityAuctionConst auctionConst) {
        if (auctionConst.equals(ActivityAuctionConst.ORDINARY_AUCTION)) {
            return cost > this.finalPrice;
        }

        return true;
    }

    /**
     * 处理拍卖操作添加记录
     * @param lordId
     * @param nickName
     * @param camp
     * @param cost
     * @param activityAuctionConst
     */
    public void purchaseAuctionItem(long lordId, String nickName, int camp, int cost, ActivityAuctionConst activityAuctionConst) {
        if (activityAuctionConst.equals(ActivityAuctionConst.DIRECT_BIDDING)) {
            this.makeDeal = true;
        }

        this.finalPrice = cost;
        this.nickName = nickName;
        this.camp = camp;
        this.ownerLordId = lordId;

        GlobalActivityAuctionItemRecord record = new GlobalActivityAuctionItemRecord();
        record.setLordId(lordId);
        record.setMakeDeal(this.makeDeal);
        record.setCostPrice(cost);
        record.setNickName(nickName);
        record.setBiddingTime(TimeHelper.getCurrentSecond());

        getRecordList().add(record);
    }

    /**
     * 反序列化
     * @param globalActivityAuctionItem
     * @return
     */
    public static GlobalActivityAuctionItem deserialization(CommonPb.GlobalActivityAuctionItem globalActivityAuctionItem) {
        GlobalActivityAuctionItem item = new GlobalActivityAuctionItem();
        item.setItemId(globalActivityAuctionItem.getItemId());
        item.setCamp(globalActivityAuctionItem.getCamp());
        item.setFinalPrice(globalActivityAuctionItem.getFinalPrice());
        item.setNickName(globalActivityAuctionItem.getNickName());
        if (!ObjectUtils.isEmpty(globalActivityAuctionItem.getRecordList())) {
            for (CommonPb.GlobalActivityAuctionItemRecord record : globalActivityAuctionItem.getRecordList()) {
                if (CheckNull.isNull(record)) {
                    continue;
                }

                item.setRecordList(Optional.ofNullable(item.getRecordList()).orElse(new ArrayList<>()));
                item.getRecordList().add(GlobalActivityAuctionItemRecord.deserialization(record));
            }
        }

        item.setOwnerLordId(globalActivityAuctionItem.getOwnerLordId());
        item.setMakeDeal(globalActivityAuctionItem.getMakeDeal());
        item.setSettle(globalActivityAuctionItem.getSettle());
        return item;
    }

    /**
     * 序列化
     * @return
     */
    public CommonPb.GlobalActivityAuctionItem serialization(boolean saveData, int myPrice) {
        CommonPb.GlobalActivityAuctionItem.Builder builder = CommonPb.GlobalActivityAuctionItem.newBuilder();
        builder.setItemId(this.itemId);
        builder.setCamp(this.camp);
        builder.setOwnerLordId(this.ownerLordId);
        builder.setFinalPrice(this.finalPrice);
        builder.setNickName(ObjectUtils.isEmpty(this.nickName) ? "" : this.nickName);
        builder.setMakeDeal(this.isMakeDeal());
        builder.setSettle(isSettle());
        builder.setMyPrice(myPrice);
        if (saveData) {
            Optional.ofNullable(this.recordList).ifPresent(records -> records.forEach(record -> {
                builder.addRecord(record.serialization(this.itemId));
            }));
        }

        return builder.build();
    }
}
