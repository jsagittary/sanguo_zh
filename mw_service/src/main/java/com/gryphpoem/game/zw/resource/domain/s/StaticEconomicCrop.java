package com.gryphpoem.game.zw.resource.domain.s;

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
     * 解锁需要的资源建筑类型
     */
    private int buildingType;

    /**
     * 解锁需要的建筑等级
     */
    private int buildingLv;

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

    public int getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(int buildingType) {
        this.buildingType = buildingType;
    }

    public int getBuildingLv() {
        return buildingLv;
    }

    public void setBuildingLv(int buildingLv) {
        this.buildingLv = buildingLv;
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
