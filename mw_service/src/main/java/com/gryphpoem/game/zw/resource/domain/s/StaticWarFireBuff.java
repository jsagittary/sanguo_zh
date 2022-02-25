package com.gryphpoem.game.zw.resource.domain.s;

/**
 * 战火燎原配置
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2021-01-27 15:56
 */
public class StaticWarFireBuff {

    public static final int BUFF_TYPE_ATTK = 1;
    public static final int BUFF_TYPE_DEF = 2;
    public static final int BUFF_TYPE_RECOVER_ARMY = 3;

    private int buffId;
    private int type; // buff类型 1 攻击 2 防御 3 伤病恢复
    private int lv; // buff的等级
    private int buffVal; // buff的数值(万分比)
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
        return "StaticWarFireBuff{" +
                "buffId=" + buffId +
                ", type=" + type +
                ", lv=" + lv +
                ", buffVal=" + buffVal +
                ", cost=" + cost +
                '}';
    }
}