package com.gryphpoem.game.zw.resource.domain.s;

import java.util.Map;

/**
 * 经济副作物
 * 
 * @Author: GeYuanpeng
 * @Date: 2022/11/11 11:47
 */
public class StaticEconomicCrop {
    
    private int id;

    /**
     * 道具id, 经济作物算作普通道具，对应s_prop表中propType为4的id
     */
    private int propId;

    /**
     * 解锁需要的建筑等级, 例如: [[33, 5], [32, 10]], 表示需要任一良田等级达到5级, 且任一伐木场等级达到10级, 可生产该经济副作物
     */
    private Map<Integer, Integer> needBuildingLv;

    /**
     * 单次产出需要的时间，秒
     */
    private int productTime;

    /**
     * 单次产出的数量
     */
    private int productCnt;

    /**
     * 拥有数量上限
     */
    private int maxCnt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPropId() {
        return propId;
    }

    public void setPropId(int propId) {
        this.propId = propId;
    }

    public Map<Integer, Integer> getNeedBuildingLv() {
        return needBuildingLv;
    }

    public void setNeedBuildingLv(Map<Integer, Integer> needBuildingLv) {
        this.needBuildingLv = needBuildingLv;
    }

    public int getProductTime() {
        return productTime;
    }

    public void setProductTime(int productTime) {
        this.productTime = productTime;
    }

    public int getProductCnt() {
        return productCnt;
    }

    public void setProductCnt(int productCnt) {
        this.productCnt = productCnt;
    }

    public int getMaxCnt() {
        return maxCnt;
    }

    public void setMaxCnt(int maxCnt) {
        this.maxCnt = maxCnt;
    }
}
