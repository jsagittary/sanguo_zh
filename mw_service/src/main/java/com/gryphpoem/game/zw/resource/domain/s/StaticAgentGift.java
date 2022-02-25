package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticAgentGift.java
 * @Description 礼物
 * @author QiuKun
 * @date 2018年6月5日
 */
public class StaticAgentGift {

    private int giftId;// 礼物ID，对应道具表
    private int IntimacyValue;// 礼物对应的亲密度
    private int price;// 礼物的购买价格
    private int awards;// 红包id
    private int claimNumber;// 可领取次数
    private int barrageId;// 需要发送弹幕的id

    public int getGiftId() {
        return giftId;
    }

    public void setGiftId(int giftId) {
        this.giftId = giftId;
    }

    public int getIntimacyValue() {
        return IntimacyValue;
    }

    public void setIntimacyValue(int intimacyValue) {
        IntimacyValue = intimacyValue;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getAwards() {
        return awards;
    }

    public void setAwards(int awards) {
        this.awards = awards;
    }

    public int getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(int claimNumber) {
        this.claimNumber = claimNumber;
    }

    public int getBarrageId() {
        return barrageId;
    }

    public void setBarrageId(int barrageId) {
        this.barrageId = barrageId;
    }

}
