package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticBerlinBuff.java
 * @Description 柏林会战官职buff
 * @author QiuKun
 * @date 2018年8月8日
 */
public class StaticBerlinBuff {
    private int buffId;
    private int type; // buff类型 1 募兵数量 2 燃油 3 电力 4 补给 5 矿石 6 建筑时间 7 行军时间 8 科技时间
    private int buffVal; // buff的数值
    private int cost; // 消耗金币的数量

    public int getBuffId() {
        return buffId;
    }

    public void setBuffId(int buffId) {
        this.buffId = buffId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getBuffVal() {
        return buffVal;
    }

    public void setBuffVal(int buffVal) {
        this.buffVal = buffVal;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    @Override
    public String toString() {
        return "StaticBerlinBuff [buffId=" + buffId + ", type=" + type + ", buffVal=" + buffVal + ", cost=" + cost
                + "]";
    }
}
