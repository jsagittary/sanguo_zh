package com.gryphpoem.game.zw.resource.pojo.buildHomeCity;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 建筑状态, 用来记录被派遣在建筑上的居民与武将
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/11 15:49
 */
public class BuildingState {

    /**
     * 建筑id
     */
    private int buildingId;

    /**
     * 被分配的农民数量
     */
    private int residentCnt;

    /**
     * 被委任的武将id
     */
    private List<Integer> heroIds = new ArrayList<>();

    /**
     * 建筑被放置的地基id
     */
    private int foundationId;

    /**
     * 被分配产出的经济作物id
     */
    private List<Integer> economicCropData = new ArrayList<>();

    /**
     * 建筑当前正在产出的经济作物 [作物id, 开始时间, 结束时间]
     */
    private List<Integer> curProductCrop = new ArrayList<>();

    private int buildingLv;

    private int buildingType;

    private int residentTopLimit;

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public int getResidentCnt() {
        return residentCnt;
    }

    public void setResidentCnt(int residentCnt) {
        this.residentCnt = residentCnt;
    }

    public List<Integer> getHeroIds() {
        return heroIds;
    }

    public void setHeroIds(List<Integer> heroIds) {
        this.heroIds = heroIds;
    }

    public int getFoundationId() {
        return foundationId;
    }

    public void setFoundationId(int foundationId) {
        this.foundationId = foundationId;
    }

    public List<Integer> getEconomicCropData() {
        return economicCropData;
    }

    public void setEconomicCropData(List<Integer> economicCropData) {
        this.economicCropData = economicCropData;
    }

    public List<Integer> getCurProductCrop() {
        return curProductCrop;
    }

    public void setCurProductCrop(List<Integer> curProductCrop) {
        this.curProductCrop = curProductCrop;
    }

    public int getBuildingLv() {
        return buildingLv;
    }

    public void setBuildingLv(int buildingLv) {
        this.buildingLv = buildingLv;
    }

    public int getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(int buildingType) {
        this.buildingType = buildingType;
    }

    public int getResidentTopLimit() {
        return residentTopLimit;
    }

    public void setResidentTopLimit(int residentTopLimit) {
        this.residentTopLimit = residentTopLimit;
    }

    public BuildingState() {
    }

    public BuildingState(int buildingId, int foundationId) {
        this.buildingId = buildingId;
        this.foundationId = foundationId;
        this.residentCnt = 0;
    }

    public BuildingState(CommonPb.BuildingState pb) {
        this.buildingId = pb.getBuildingId();
        this.residentCnt = pb.getResidentCnt();
        this.heroIds = pb.getHeroIdList();
        this.foundationId = pb.getFoundationId();
        this.economicCropData = pb.getEconomicCropIdList();
        CommonPb.EconomicCropInfo economicCropInfo = pb.getEconomicCropInfo();
        this.curProductCrop.add(economicCropInfo.getCropId());
        this.curProductCrop.add(economicCropInfo.getStartTime());
        this.curProductCrop.add(economicCropInfo.getEndTime());
        this.buildingLv = pb.getBuildingLv();
        this.buildingType = pb.getBuildingType();
    }

    public CommonPb.BuildingState creatPb() {
        CommonPb.BuildingState.Builder pb = CommonPb.BuildingState.newBuilder();
        pb.setBuildingId(this.getBuildingId());
        pb.setResidentCnt(this.getResidentCnt());
        pb.addAllHeroId(this.getHeroIds());
        pb.setFoundationId(this.getFoundationId());
        pb.addAllEconomicCropId(this.economicCropData);
        if (CheckNull.nonEmpty(this.curProductCrop) && this.curProductCrop.size() >= 3) {
            CommonPb.EconomicCropInfo.Builder economicCropInfo = CommonPb.EconomicCropInfo.newBuilder();
            economicCropInfo.setCropId(this.curProductCrop.get(0));
            economicCropInfo.setStartTime(this.curProductCrop.get(1));
            economicCropInfo.setEndTime(this.curProductCrop.get(2));
        }
        pb.setBuildingLv(this.buildingLv);
        pb.setBuildingType(this.buildingType);

        return pb.build();
    }

}
