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
     * 2 攻打匪军
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
     * 共可获取的掉落数量
     */
    private int total;

    /**
     * 活动结束后，掉落物品的转换<br/>
     * 数量上总是1：1进行转换的
     */
    private List<List<Integer>> convert;

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
}
