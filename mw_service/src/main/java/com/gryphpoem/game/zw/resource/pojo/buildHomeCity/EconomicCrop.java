package com.gryphpoem.game.zw.resource.pojo.buildHomeCity;

/**
 * 建筑被分配产出的经济作物信息
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/15 13:57
 */
public class EconomicCrop {

    /**
     * 经济作物id, 对应prop表的id
     */
    private int economicCropId;

    /**
     * 开始产出时间
     */
    private int startTime;


    /**
     * 产出数量
     */
    private int productCnt;

    public EconomicCrop(int economicCropId, int startTime, int productCnt) {
        this.economicCropId = economicCropId;
        this.startTime = startTime;
        this.productCnt = productCnt;
    }

    public int getEconomicCropId() {
        return economicCropId;
    }

    public void setEconomicCropId(int economicCropId) {
        this.economicCropId = economicCropId;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getProductCnt() {
        return productCnt;
    }

    public void setProductCnt(int productCnt) {
        this.productCnt = productCnt;
    }

    @Override
    public String toString() {
        return "EconomicCrop{" +
                "economicCropId=" + economicCropId +
                ", startTime=" + startTime +
                ", productCnt=" + productCnt +
                '}';
    }
}
