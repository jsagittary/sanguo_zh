package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticActGiftpack.java
 * @Description 活动礼包奖励表
 * @author QiuKun
 * @date 2017年8月14日
 */
public class StaticActGiftpack {
    private int giftpackId; // 礼包的id,对应的payid
    private List<List<Integer>> award;// 礼包内容
    private int topup;// 购买礼包延迟到账但超过了数量时以充值货币形式发给玩家
    private int count;// 礼包购买次数
    private int displayGold;// 金币原价
    private int gold;// 金币价格

    public int getDisplayGold() {
        return displayGold;
    }

    public void setDisplayGold(int displayGold) {
        this.displayGold = displayGold;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getGiftpackId() {
        return giftpackId;
    }

    public void setGiftpackId(int giftpackId) {
        this.giftpackId = giftpackId;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public int getTopup() {
        return topup;
    }

    public void setTopup(int topup) {
        this.topup = topup;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
