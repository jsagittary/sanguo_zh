package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2019-04-18 14:13
 * @description: 匪军掉落活动
 * @modified By:
 */
public class StaticActBandit {

    /**
     * 活动掉落
     * 击飞玩家
     */
    public static final int ACT_HIT_DROP_TYPE_1 = 1;
    /**
     * 活动掉落
     * 攻打匪军
     */
    public static final int ACT_HIT_DROP_TYPE_2 = 2;
    //精英叛军
    public static final int ACT_HIT_DROP_TYPE_3 = 3;

    private int id;

    /**
     * 1 击飞玩家<br>
     * 2 攻打匪军<br>
     * 4 采集掉落
     */
    private int type;

    /**
     * 活动类型
     */
    private int activityType;

    /**
     * 活动id
     */
    private int activityId;

    /**
     * 掉落格式: [[]]
     */
    private List<List<Integer>> drop;

    /**
     * 单日可获取数量上限
     */
    private int total;

    /**
     * 活动结束后，掉落物品的转换<br/>
     * 数量上总是1：1进行转换的
     */
    private List<List<Integer>> convert;

    /**
     * 单次采集每满XXX秒可掉落道具
     */
    private int dropTime;

    /**
     * 资源点id区间：[[104,111],[204,211]]
     */
    private List<List<Integer>> mineId;

    /**
     * 单次可获取数量上限
     */
    private int onceTotal;

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

    public int getActivityType() {
        return activityType;
    }

    public void setActivityType(int activityType) {
        this.activityType = activityType;
    }

    public List<List<Integer>> getDrop() {
        return drop;
    }

    public void setDrop(List<List<Integer>> drop) {
        this.drop = drop;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<List<Integer>> getConvert() {
        return convert;
    }

    public void setConvert(List<List<Integer>> convert) {
        this.convert = convert;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getDropTime() {
        return dropTime;
    }

    public void setDropTime(int dropTime) {
        this.dropTime = dropTime;
    }

    public List<List<Integer>> getMineId() {
        return mineId;
    }

    public void setMineId(List<List<Integer>> mineId) {
        this.mineId = mineId;
    }

    public int getOnceTotal() {
        return onceTotal;
    }

    public void setOnceTotal(int onceTotal) {
        this.onceTotal = onceTotal;
    }
}
