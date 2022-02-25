package com.gryphpoem.game.zw.resource.domain.p;

/**
 * 效果加成
 * 
 * @author tyler
 *
 */
public class Effect {
    private int effectType;// 效果类型(1护盾, 2攻击加成, 3防御加成, 4行军加速, 5募兵加速 )
    private int effectVal;// 效果值
    private int endTime;// 结束时间

    public Effect(int effectType, int effectVal, int endTime) {
        this.effectType = effectType;
        this.effectVal = effectVal;
        this.endTime = endTime;
    }

    public int getEffectType() {
        return effectType;
    }

    public void setEffectType(int effectType) {
        this.effectType = effectType;
    }

    public int getEffectVal() {
        return effectVal;
    }

    public void setEffectVal(int effectVal) {
        this.effectVal = effectVal;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Effect [effectType=" + effectType + ", effectVal=" + effectVal + ", endTime=" + endTime + "]";
    }

}
