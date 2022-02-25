package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author QiuKun
 * @ClassName StaticCastleSkin.java
 * @Description 城堡皮肤
 * @date 2019年4月14日
 */
public class StaticCastleSkin {

    /**
     * 采集加成效果
     */
    public static final int EFFECT_TYPE_COLLECT = 1;
    /**
     * 纽约争霸战个人排行奖励，增加20%
     */
    public static final int EFFECT_TYPE_NEW_YORK_WAR = 2;
    /**
     * 将领属性增加 固定值
     */
    public static final int EFFECT_TYPE_HERO_ATTR = 3;
    /**
     * 行军加速
     */
    public static final int EFFECT_TYPE_WALK_SPEED = 4;

    /**
     * 默认皮肤id
     */
    public static final int DEFAULT_SKIN_ID = 1;

    /**
     * 柏林霸主皮肤id
     */
    public static final int BERLIN_WINNER_SKIN_ID = 13;

    /**
     * 柏林霸主称号id
     */
    public static final int BERLIN_WINNER_TITLE_ID = 200;

    /**
     * 战火燎原排名前3称号id
     */
    public static final int WAR_FIRE_WINNER_TITLE_ID = 201;
    
    /**
     * 皮肤 拥有时间类型 1 永久类型
     */
    public static final int SKIN_TIME_TYPE = 1;
    /**
     * 皮肤 最小拥有时间 秒
     */
    public static final int SKIN_TIME_MIN_DURATION = 3600;

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
     * 如果{@link StaticCastleSkin#effectType} == 2 的时候才使用兵种类型判断
     */
    private int armyType;
    /**
     * 初始星级
     */
    private int star;

    public List<List<Integer>> getConsume() {
        return consume;
    }

    public void setConsume(List<List<Integer>> consume) {
        this.consume = consume;
    }

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

    @Override
    public String toString() {
        return "StaticCastleSkin{" +
                "id=" + id +
                ", effectType=" + effectType +
                ", effectParam=" + effectParam +
                ", effectVal=" + effectVal +
                ", timeType=" + timeType +
                ", duration=" + duration +
                ", armyType=" + armyType +
                ", star=" + star +
                ", consume=" + consume +
                '}';
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }
}
