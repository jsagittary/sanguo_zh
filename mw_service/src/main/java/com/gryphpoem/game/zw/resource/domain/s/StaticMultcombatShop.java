package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticMultcombatShop.java
 * @Description
 * @author QiuKun
 * @date 2018年12月26日
 */
public class StaticMultcombatShop {
    private int id;// 商品id
    private int cost;// 消耗的对应副本点数
    private List<Integer> award;// 兑换所得物品
    private int buyCnt; // 道具可购买次数 填0表示不生效
    private int combatIdCond; // 购买物品时需要的通关副本id的条件 填0表示不生效

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public List<Integer> getAward() {
        return award;
    }

    public void setAward(List<Integer> award) {
        this.award = award;
    }

    public int getBuyCnt() {
        return buyCnt;
    }

    public void setBuyCnt(int buyCnt) {
        this.buyCnt = buyCnt;
    }

    public int getCombatIdCond() {
        return combatIdCond;
    }

    public void setCombatIdCond(int combatIdCond) {
        this.combatIdCond = combatIdCond;
    }

    /**
     * 购买次数条件是否生效
     * 
     * @return true 生效
     */
    public boolean isBuyCntEnable() {
        return buyCnt > 0;
    }

    /**
     * 副本id条件是否生效
     * 
     * @return true 生效
     */
    public boolean isCombatIdCondEnable() {
        return combatIdCond > 0;
    }

}
