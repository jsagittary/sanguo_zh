package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.resource.util.CheckNull;

public enum ActivityAuctionConst {
    /**
     * 普通竞拍
     */
    ORDINARY_AUCTION(100),
    /**
     * 一口价拿下
     */
    DIRECT_BIDDING(101),

    //======================**************===================
//    /**
//     * 活动还未开始
//     */
//    ACT_PREVIEW(200),
    /**
     * 展示中
     */
    ROUND_ON_DISPLAY(201),
    /**
     * 售卖中
     */
    ON_SALE(202),
    /**
     * 回合结束
     */
    ACT_END(203),

    //======================================================
    /**
     * 关注
     */
    FOCUS_ON(300),
    /**
     * 取关
     */
    UNSUBSCRIBE(301),

    //======================================================
    /**
     * 关注的列表
     */
    FOLLOWED_PROPS_LIST(400, 0),
    /**
     * 装备列表
     */
    EQUIP_PROPS_LIST(401, 1),
    /**
     * 加速的列表
     */
    ACCELERATION_PROPS_LIST(402, 2),
    /**
     * 战争道具列表
     */
    WAR_PROPS_LIST(403, 3),
    /**
     * 其他道具的列表
     */
    OTHER_PROPS_LIST(404, 4),

    //==============================================================================
    SAVE_RECORD_DATA(500),
    NOT_NEED_RECORDS(501),
    SYNC_ONE_ITEM_CHANGE(502),


    ;
    private int type;

    private int itemType;

    public int getItemType() {
        return itemType;
    }

    public int getType() {
        return type;
    }

    ActivityAuctionConst(int type) {
        this.type = type;
        this.itemType = -1;
    }

    ActivityAuctionConst(int type, int itemType) {
        this.type = type;
        this.itemType = itemType;
    }

    public static ActivityAuctionConst convertTo(int type) {
        for (ActivityAuctionConst auctionConst : values()) {
            if (type == auctionConst.getType()) {
                return auctionConst;
            }
        }

        return null;
    }

    public static ActivityAuctionConst convertItemTypeTo(int itemType) {
        for (ActivityAuctionConst auctionConst : values()) {
            if (itemType == auctionConst.getItemType()) {
                return auctionConst;
            }
        }

        return null;
    }

    public static ActivityAuctionConst inType(int type) {
        ActivityAuctionConst auctionConst = convertItemTypeTo(type);
        if (CheckNull.isNull(auctionConst)) {
            return null;
        }

        switch (auctionConst) {
            case FOLLOWED_PROPS_LIST:
            case EQUIP_PROPS_LIST:
            case ACCELERATION_PROPS_LIST:
            case WAR_PROPS_LIST:
            case OTHER_PROPS_LIST:
                return auctionConst;
            default:
                return null;
        }
    }

    public static ActivityAuctionConst purchase(int type) {
        ActivityAuctionConst auctionConst = convertTo(type);
        if (CheckNull.isNull(auctionConst)) {
            return null;
        }

        switch (auctionConst) {
            case DIRECT_BIDDING:
            case ORDINARY_AUCTION:
                return auctionConst;
            default:
                return null;
        }
    }
}
