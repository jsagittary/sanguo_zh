package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityAuctionItem;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import org.springframework.util.ObjectUtils;

import java.util.*;

public class ActivityAuction {
    /**
     * 最高竞拍价次数
     */
    private int highestBidCount;
    /**
     * 玩家关注的拍卖品
     */
    private Set<Integer> concernedItem;
    /**
     * 竞拍的钻石消耗
     */
    private Map<Integer, Integer> costDiamond;
    /**
     * 个人竞拍日志
     */
    private List<ActivityAuctionRecord> recordList;

    /**
     * 每一种类型最高出价次数
     */
    private Map<Integer, Integer> typeHighestBidCountCount;

    public Set<Integer> getConcernedItem() {
        if (CheckNull.isNull(concernedItem)) {
            concernedItem = new HashSet<>();
        }
        return concernedItem;
    }

    public void setConcernedItem(Set<Integer> concernedItem) {
        this.concernedItem = concernedItem;
    }

    public Map<Integer, Integer> getCostDiamond() {
        if (ObjectUtils.isEmpty(this.costDiamond)) {
            costDiamond = new HashMap<>();
        }
        return costDiamond;
    }

    public void setCostDiamond(Map<Integer, Integer> costDiamond) {
        this.costDiamond = costDiamond;
    }

    public List<ActivityAuctionRecord> getRecordList() {
        if (CheckNull.isNull(this.recordList)) {
            this.recordList = new ArrayList<>();
        }
        return recordList;
    }

    public void setRecordList(List<ActivityAuctionRecord> recordList) {
        this.recordList = recordList;
    }

    public int getHighestBidCount() {
        return highestBidCount;
    }

    public void setHighestBidCount(int highestBidCount) {
        this.highestBidCount = highestBidCount;
    }

    public Map<Integer, Integer> getTypeHighestBidCountCount() {
        if (ObjectUtils.isEmpty(typeHighestBidCountCount)) {
            this.typeHighestBidCountCount = new HashMap<>();
        }

        return typeHighestBidCountCount;
    }

    public void setTypeHighestBidCountCount(Map<Integer, Integer> typeHighestBidCountCount) {
        this.typeHighestBidCountCount = typeHighestBidCountCount;
    }

    public int getMyPrice(GlobalActivityAuctionItem item, long mineLordId) {
        Integer cost = getCostDiamond().getOrDefault(item.getItemId(), 0);
        if (item.isMakeDeal() && item.getOwnerLordId() == mineLordId) {
            cost = item.getFinalPrice();
        }

        return cost;
    }

    /**
     * 增加或减少最高价保持者次数
     * @param add
     */
    public void addHighestBidCount(boolean add, int type) {
        int typeCount = getTypeHighestBidCountCount().getOrDefault(type, 0);
        getTypeHighestBidCountCount().put(type, add ? ++typeCount : (typeCount == 0 ? 0 : --typeCount));

        this.highestBidCount += add ? 1 : (this.highestBidCount == 0 ? 0 : -1);
    }

    /**
     * 回合结束结算
     * @return
     */
    public Integer settle() {
        this.highestBidCount = 0;
        this.getConcernedItem().clear();
        this.getRecordList().clear();
        this.getTypeHighestBidCountCount().clear();

        if (ObjectUtils.isEmpty(this.costDiamond)) {
            return 0;
        }

        int settle = this.costDiamond.values().stream().mapToInt(Integer::intValue).sum();
        this.costDiamond.clear();
        return settle;
    }

    /**
     * 拍卖回合结束结算个人活动信息
     * @param itemId
     * @return
     */
    public Integer returnCost(int itemId) {
        return this.getCostDiamond().remove(itemId);
    }

    public void clean() {
        this.highestBidCount = 0;
        this.getConcernedItem().clear();
        this.getCostDiamond().clear();
        this.getRecordList().clear();
        this.getTypeHighestBidCountCount().clear();
    }

    /**
     * 出价拍卖品 记录操作
     * @param lordId
     * @param nickName
     * @param cost
     * @param itemId
     * @param auctionConst
     * @param mine
     */
    public void purchase(long lordId, String nickName, int cost, int itemId, ActivityAuctionConst auctionConst, int recordCost, int now, boolean mine) {
        if (mine && auctionConst.equals(ActivityAuctionConst.ORDINARY_AUCTION)) {
            getCostDiamond().put(itemId, cost);
        }

        ActivityAuctionRecord record = new ActivityAuctionRecord();
        record.setItemId(itemId);
        record.setLordId(lordId);
        record.setNickName(nickName);
        record.setCostDiamond(recordCost);
        record.setStatus(auctionConst);
        record.setLogTime(now);
        getRecordList().add(record);
    }

    public boolean operation(Integer itemId, ActivityAuctionConst auctionConst) {
        switch (auctionConst) {
            case FOCUS_ON:
                return !getConcernedItem().contains(itemId);
            case UNSUBSCRIBE:
                return getConcernedItem().contains(itemId);
            default:
                break;
        }

        return false;
    }

    /**
     * 关注（取关）拍卖品
     * @param auctionConst
     * @param itemId
     */
    public void followAuctions(ActivityAuctionConst auctionConst, Integer itemId) {
        switch (auctionConst) {
            case FOCUS_ON:
                getConcernedItem().add(itemId);
                break;
            case UNSUBSCRIBE:
                getConcernedItem().remove(itemId);
                break;
            default:
                break;
        }
    }

    /**
     * 反序列化
     * @param activityAuctionPb
     */
    public void deserialization(SerializePb.ActivityAuction activityAuctionPb) {
        if (CheckNull.isNull(activityAuctionPb)) {
            return;
        }

        this.highestBidCount = activityAuctionPb.getHighestBidCount();
        Optional.ofNullable(activityAuctionPb.getConcernedItemList()).ifPresent(concernedItemList -> this.getConcernedItem().addAll(concernedItemList));
        Optional.ofNullable(activityAuctionPb.getCostDiamondList()).ifPresent(twoIntList -> twoIntList.forEach(twoInt -> {
            if (CheckNull.isNull(twoInt))
                return;

            this.getCostDiamond().put(twoInt.getV1(), twoInt.getV2());
        }));

        Optional.ofNullable(activityAuctionPb.getRecordList()).ifPresent(recordList -> recordList.forEach(record -> {
            ActivityAuctionRecord activityAuctionRecord = new ActivityAuctionRecord();
            activityAuctionRecord.deserialization(record);
            this.getRecordList().add(activityAuctionRecord);
        }));
        Optional.ofNullable(activityAuctionPb.getTypeHighestBidCountCountList()).ifPresent(list -> list.forEach(twoInt -> {
            if (CheckNull.isNull(twoInt))
                return;

            this.getTypeHighestBidCountCount().put(twoInt.getV1(), twoInt.getV2());
        }));
    }

    /**
     * 序列化
     * @return
     */
    public SerializePb.ActivityAuction serialization() {
        SerializePb.ActivityAuction.Builder builder = SerializePb.ActivityAuction.newBuilder();
        builder.setHighestBidCount(this.highestBidCount);
        builder.addAllConcernedItem(this.getConcernedItem());
        Optional.ofNullable(this.costDiamond).ifPresent(costDiamondList -> costDiamondList.forEach((itemId, cost) -> {
            CommonPb.TwoInt.Builder twoIntPb = CommonPb.TwoInt.newBuilder();
            twoIntPb.setV1(itemId);
            twoIntPb.setV2(cost);

            builder.addCostDiamond(twoIntPb);
        }));

        Optional.ofNullable(this.recordList).ifPresent(list -> list.forEach(record -> {
            builder.addRecord(record.serialization());
        }));

        Optional.ofNullable(this.typeHighestBidCountCount).ifPresent(typeCountMap -> typeCountMap.forEach((type, count) -> {
            CommonPb.TwoInt.Builder twoIntPb = CommonPb.TwoInt.newBuilder();
            twoIntPb.setV1(type);
            twoIntPb.setV2(count);

            builder.addTypeHighestBidCountCount(twoIntPb);
        }));
        return builder.build();
    }
}
