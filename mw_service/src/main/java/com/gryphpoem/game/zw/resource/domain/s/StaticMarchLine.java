package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @description:
 * @author: zhou jie
 * @time: 2021/9/13 20:17
 */
public class StaticMarchLine {

    /**
     * 将领属性增加 固定值
     */
    public static final int EFFECT_TYPE_HERO_ATTR = 3;

    /**
     * 行军加速
     */
    public static final int EFFECT_TYPE_WALK_SPEED = 4;

    /**
     * 配置id
     */
    private int id;
    /**
     * 皮肤效果类型, 1.采集加成效果
     */
    private int effectType;
    /**
     * 效果的参数
     */
    private List<Integer> effectParam;
    /**
     * 属性值
     */
    private int effectVal;
    /**
     * 时间类型（1 永久类型 0 时效限制）
     */
    private int timeType;
    /**
     * 拥有时间
     */
    private int duration;
    /**
     * 兵种类型
     */
    private int armyType;
    /**
     * 转换道具
     */
    private List<List<Integer>> consume;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEffectType() {
        return effectType;
    }

    public void setEffectType(int effectType) {
        this.effectType = effectType;
    }

    public List<Integer> getEffectParam() {
        return effectParam;
    }

    public void setEffectParam(List<Integer> effectParam) {
        this.effectParam = effectParam;
    }

    public int getEffectVal() {
        return effectVal;
    }

    public void setEffectVal(int effectVal) {
        this.effectVal = effectVal;
    }

    public int getTimeType() {
        return timeType;
    }

    public void setTimeType(int timeType) {
        this.timeType = timeType;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getArmyType() {
        return armyType;
    }

    public void setArmyType(int armyType) {
        this.armyType = armyType;
    }

    public List<List<Integer>> getConsume() {
        return consume;
    }

    public void setConsume(List<List<Integer>> consume) {
        this.consume = consume;
    }
}
