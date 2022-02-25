package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticCrossBuff.java
 * @Description
 * @author QiuKun
 * @date 2019年5月15日
 */
public class StaticCrossBuff {
    private int buffId;
    private int type; // buff类型 1攻击 2 防御 35 穿甲 36 防护
    private int lv; // buff的等级
    private int buffVal; // buff的数值 
    private int cost;// 消耗的金币数

    public static String mapKey(int type, int lv) {
        return type + "_" + lv;
    }

    public String getMapKey() {
        return mapKey(this.type, this.lv);
    }

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

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
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
        return "StaticCrossBuff [buffId=" + buffId + ", type=" + type + ", lv=" + lv + ", buffVal=" + buffVal
                + ", cost=" + cost + "]";
    }

}
