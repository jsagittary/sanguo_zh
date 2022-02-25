package com.gryphpoem.game.zw.resource.domain.s;

/**
 * 飞艇的buff表
 * @program: empire_en
 * @description:
 * @author: zhou jie
 * @create: 2020-07-11 12:03
 */
public class StaticAirShipBuff {

    /**
     * buffId, 主键
     */
    private int buffId;
    /**
     * buff类型
     */
    private int type;
    /**
     * buff的数值(万分比)
     */
    private int buffVal;
    /**
     * 持续时间(秒)
     */
    private int time;

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

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "StaticAirShipBuff{" +
                "buffId=" + buffId +
                ", type=" + type +
                ", buffVal=" + buffVal +
                ", time=" + time +
                '}';
    }
}