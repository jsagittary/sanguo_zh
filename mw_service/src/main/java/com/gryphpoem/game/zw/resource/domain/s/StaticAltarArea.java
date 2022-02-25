package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.util.MapHelper;

import java.util.List;

/**
 * @description:
 * @author: zhou jie
 * @time: 2021/7/21 15:21
 */
public class StaticAltarArea {
    /**
     * 主键
     */
    private int id;
    /**
     * 活动id
     */
    private int activityId;
    /**
     * 区域开放顺序
     */
    private int areaOrder;
    /**
     * 上部分区域刷新数量
     */
    private int areaUp;
    /**
     * 下部分区域刷新数量
     */
    private int areaDown;
    /**
     * 黄金参观奖励
     */
    private List<List<Integer>> goldAward;
    /**
     * 钻石拜访奖励
     */
    private List<List<Integer>> diamondAward;
    /**
     * 互动次数上限
     */
    private int upperLimit;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAreaOrder() {
        return areaOrder;
    }

    public void setAreaOrder(int areaOrder) {
        this.areaOrder = areaOrder;
    }

    public int getAreaUp() {
        return areaUp;
    }

    public void setAreaUp(int areaUp) {
        this.areaUp = areaUp;
    }

    public int getAreaDown() {
        return areaDown;
    }

    public void setAreaDown(int areaDown) {
        this.areaDown = areaDown;
    }

    public List<List<Integer>> getGoldAward() {
        return goldAward;
    }

    public void setGoldAward(List<List<Integer>> goldAward) {
        this.goldAward = goldAward;
    }

    public List<List<Integer>> getDiamondAward() {
        return diamondAward;
    }

    public void setDiamondAward(List<List<Integer>> diamondAward) {
        this.diamondAward = diamondAward;
    }

    public int getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(int upperLimit) {
        this.upperLimit = upperLimit;
    }

    /**
     * 根据刷新区域获取配置
     *
     * @param half 刷新半区
     * @return 刷新数量
     */
    public int getHalfInAreaConf(int half) {
        return half == MapHelper.UP_HALF_IN_AREA ? areaUp : areaDown;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }
}
