package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengshuo on 2019/4/13 11:33
 * <br>Description: 巴顿活动单个格子中的数据
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class ActBartonItem {
    /** 活动的activityId */
    private int activityId;
    /** keyId */
    private int keyId;
    /** 格子编号 */
    private int cond;
    /** 物品 [type,id,count] */
    private List<Integer> award;
    /** 是否已购买 true为已购买 */
    private boolean isPurchased;
    /** 原价 */
    private int price;
    /** 折扣 90表示9折 */
    private int discount;
    /** 折后价格 */
    private int discountPrice;

    public ActBartonItem() {
        award = new ArrayList<>();
    }

    ActBartonItem(CommonPb.ActBartonItem e) {
        this();
        setActivityId(e.getActivityId());
        setKeyId(e.getKeyId());
        setCond(e.getCond());
        setPurchased(e.getIsPurchased());
        setPrice(e.getPrice());
        setDiscount(e.getDiscount());
        setDiscountPrice(e.getDiscountPrice());
        award.addAll(e.getAwardList());
    }

    public CommonPb.ActBartonItem dser() {
        CommonPb.ActBartonItem.Builder builder = CommonPb.ActBartonItem.newBuilder();
        builder.setActivityId(activityId);
        builder.setKeyId(keyId);
        builder.setCond(cond);
        builder.setIsPurchased(isPurchased);
        builder.setPrice(price);
        builder.setDiscount(discount);
        builder.setDiscountPrice(discountPrice);
        builder.addAllAward(award);
        return builder.build();
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
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

    public void setPurchased(boolean purchased) {
        isPurchased = purchased;
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

    @Override
    public String toString() {
        return "ActBartonItem{" +
                "activityId=" + activityId +
                ", keyId=" + keyId +
                ", cond=" + cond +
                ", award=" + award +
                ", isPurchased=" + isPurchased +
                ", price=" + price +
                ", discount=" + discount +
                ", discountPrice=" + discountPrice +
                '}';
    }

}
