package com.gryphpoem.game.zw.resource.domain.p;

import java.util.ArrayList;
import java.util.List;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @ClassName ActBlackhawkItem.java
 * @Description 黑鹰计划获取单个格子的数据
 * @author QiuKun
 * @date 2017年7月10日
 */
public class ActBlackhawkItem {

    private int keyId;
    private int cond;
    private List<Integer> award; // 奖励的物品
    private boolean isPurchased; // 是否已购买 true为已购买
    private int price; // 原价
    private int discount; // 折扣 90表示9折,100表示没有折扣
    private int discountPrice; // 折后价格

    public ActBlackhawkItem() {
        award = new ArrayList<>();
    }

    public ActBlackhawkItem(CommonPb.BlackhawkItem ser) {
        this();
        setKeyId(ser.getKeyId());
        setCond(ser.getCond());
        setPrice(ser.getPrice());
        setPurchased(ser.getIsPurchased());
        setDiscount(ser.getDiscount());
        setDiscountPrice(ser.getDiscountPrice());
        for (Integer i : ser.getAwardList()) {
            award.add(i);
        }
    }

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

    public List<Integer> getAward() {
        return award;
    }

    public void setAward(List<Integer> award) {
        this.award = award;
    }

    public boolean isPurchased() {
        return isPurchased;
    }

    public void setPurchased(boolean isPurchased) {
        this.isPurchased = isPurchased;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public int getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(int discountPrice) {
        this.discountPrice = discountPrice;
    }

}
