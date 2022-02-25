package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticMentorShop.java
 * @Description 荣耀演练场商店
 * @author QiuKun
 * @date 2018年12月3日
 */
public class StaticMentorShop {

    private int id;// 商品id
    private int type;// 副本类型
    private int costPoint;// 消耗的对应副本点数
    private List<Integer> award;// 兑换所得物品
    private int buyCnt; // 道具可购买次数 填0表示不生效
    private int combatIdCond; // 购买物品时需要的通关副本id的条件 填0表示不生效

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

    public int getCostPoint() {
        return costPoint;
    }

    public void setCostPoint(int costPoint) {
        this.costPoint = costPoint;
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

    @Override
    public String toString() {
        return "StaticMentorShop [id=" + id + ", type=" + type + ", costPoint=" + costPoint + ", award=" + award
                + ", buyCnt=" + buyCnt + ", combatIdCond=" + combatIdCond + "]";
    }

}
