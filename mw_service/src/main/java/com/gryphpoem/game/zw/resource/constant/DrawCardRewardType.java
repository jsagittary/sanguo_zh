package com.gryphpoem.game.zw.resource.constant;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-14 9:33
 */
public enum DrawCardRewardType {
    /** 橙色武将*/
    ORANGE_HERO(1),
    /** 紫色武将*/
    PURPLE_HERO(2),
    /**  橙色武将碎片*/
    ORANGE_HERO_FRAGMENT(3),
    /**  紫色武将碎片*/
    PURPLE_HERO_FRAGMENT(4),
    /**  道具奖励*/
    PROP_REWARD(5),;

    private int type;

    public int getType() {
        return type;
    }

    DrawCardRewardType(int type) {
        this.type = type;
    }

    public static DrawCardRewardType convertTo(int type) {
        for (DrawCardRewardType tmp : values()) {
            if (tmp.getType() == type)
                return tmp;
        }
        return null;
    }
}
