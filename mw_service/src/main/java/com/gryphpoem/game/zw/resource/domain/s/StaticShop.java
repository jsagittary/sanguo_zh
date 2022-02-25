package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author tyler
 *
 */
public class StaticShop {
    private int id;//
    private int type;
    private List<Integer> award;
    private int price;
    private int vipPrice;
    private Map<Integer, Integer> vipCnt;
    private Map<Integer, Integer> vipFreeCnt;
    private boolean isIimit;
    private int buyCnt;
    private int needVip;
    private int tradeItemId;
    private int needRoleLv;

    public int getNeedRoleLv() {
        return needRoleLv;
    }

    public void setNeedRoleLv(int needRoleLv) {
        this.needRoleLv = needRoleLv;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getVipPrice() {
        return vipPrice;
    }

    public void setVipPrice(int vipPrice) {
        this.vipPrice = vipPrice;
    }

    public Map<Integer, Integer> getVipCnt() {
        return vipCnt;
    }

    public int getVipCnt(int vip) {
        return vipCnt.get(vip) != null ? vipCnt.get(vip) : 0;
    }

    public void setVipCnt(Map<Integer, Integer> vipCnt) {
        this.vipCnt = vipCnt;
    }

    public boolean isIimit() {
        return isIimit;
    }

    public void setIimit(boolean isIimit) {
        this.isIimit = isIimit;
    }

    public int getBuyCnt() {
        return buyCnt;
    }

    public void setBuyCnt(int buyCnt) {
        this.buyCnt = buyCnt;
    }

    public int getNeedVip() {
        return needVip;
    }

    public void setNeedVip(int needVip) {
        this.needVip = needVip;
    }

    public int getTradeItemId() {
        return tradeItemId;
    }

    public void setTradeItemId(int tradeItemId) {
        this.tradeItemId = tradeItemId;
    }

    public List<Integer> getAward() {
        return award;
    }

    public void setAward(List<Integer> award) {
        this.award = award;
    }

    public Map<Integer, Integer> getVipFreeCnt() {
        return vipFreeCnt;
    }

    public void setVipFreeCnt(Map<Integer, Integer> vipFreeCnt) {
        this.vipFreeCnt = vipFreeCnt;
    }

}
