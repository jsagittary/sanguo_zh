package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticCreditShop.java
 * @Description 好友积分商城配置
 * @author QiuKun
 * @date 2017年7月3日
 */
public class StaticCreditShop {
    private int id;// 商品id
    private List<Integer> award;// 获得物品 格式:[type,id,count]
    private int price;// 消耗积分
    private int needlv;// 需要的等级
    private int isRepeat; // 是否可重复购买,0 可重复购买,1只能单次购买

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getAward() {
        return award;
    }

    public void setAward(List<Integer> award) {
        this.award = award;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getNeedlv() {
        return needlv;
    }

    public void setNeedlv(int needlv) {
        this.needlv = needlv;
    }

    public int getIsRepeat() {
        return isRepeat;
    }

    public void setIsRepeat(int isRepeat) {
        this.isRepeat = isRepeat;
    }

}
