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
     * 建筑类型
     */
    private int buildingType;

    /**
     * 建筑等级
     */
    private int buildingLv;

    /**
     * 建筑被放置的地基id
     */
    private int foundationId;

    /**
     * 建筑可被分配的居民上限, 用于派遣, 方便计算派遣的优先级
     */
    private int residentTopLimit;

    /**
     * 被分配的农民数量
     */
    private int residentCnt;

    /**
     * 被委任的武将id
     */
    private List<Integer> heroIds = new ArrayList<>();

    /**
     * 被分配产出的经济作物id
     */
    private List<Integer> economicCropData = new ArrayList<>();

    /**
     * 建筑当前正在产出的经济作物 [作物id, 开始时间, 结束时间]
     */
    private List<Integer> curProductCrop = new ArrayList<>();

    /**
     * 地貌类型, 非0值对应s_foundation_buff的landType, 为0则表示没有地貌bUff
     */
    private int landType;

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
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

    public int getFoundationId() {
        return foundationId;
    }

    public void setFoundationId(int foundationId) {
        this.foundationId = foundationId;
    }

    public int getResidentTopLimit() {
        return residentTopLimit;
    }

    public void setResidentTopLimit(int residentTopLimit) {
        this.residentTopLimit = residentTopLimit;
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

    public int getLandType() {
        return landType;
    }

    public void setLandType(int landType) {
        this.landType = landType;
    }

    public BuildingState() {
    }

    public BuildingState(int buildingId, int buildingType) {
        this.buildingId = buildingId;
        this.buildingType = buildingType;
    }

    public BuildingState(CommonPb.BuildingState pb) {
        this.buildingId = pb.getBuildingId();
        this.buildingLv = pb.getBuildingLv();
        this.buildingType = pb.getBuildingType();
        this.foundationId = pb.getFoundationId();
        this.residentTopLimit = pb.getResidentTopLimit();
        this.residentCnt = pb.getResidentCnt();
        this.heroIds = pb.getHeroIdList();
        this.economicCropData = pb.getEconomicCropIdList();
        if (pb.hasEconomicCropInfo()) {
            CommonPb.EconomicCropInfo economicCropInfo = pb.getEconomicCropInfo();
            this.curProductCrop.add(economicCropInfo.getCropId());
            this.curProductCrop.add(economicCropInfo.getStartTime());
            this.curProductCrop.add(economicCropInfo.getEndTime());
        }
    }

    public CommonPb.BuildingState creatPb() {
        CommonPb.BuildingState.Builder pb = CommonPb.BuildingState.newBuilder();
        pb.setBuildingId(this.getBuildingId());
        pb.setBuildingLv(this.buildingLv);
        pb.setBuildingType(this.buildingType);
        pb.setFoundationId(this.foundationId);
        pb.setResidentTopLimit(this.residentTopLimit);
        pb.setResidentCnt(this.residentCnt);
        pb.addAllHeroId(this.heroIds);
        pb.addAllEconomicCropId(this.economicCropData);
        if (CheckNull.nonEmpty(this.curProductCrop) && this.curProductCrop.size() >= 3) {
            CommonPb.EconomicCropInfo.Builder economicCropInfo = CommonPb.EconomicCropInfo.newBuilder();
            economicCropInfo.setCropId(this.curProductCrop.get(0));
            economicCropInfo.setStartTime(this.curProductCrop.get(1));
            economicCropInfo.setEndTime(this.curProductCrop.get(2));
            pb.setEconomicCropInfo(economicCropInfo.build());
        }

        return pb.build();
    }

    @Override
    public String toString() {
        return "BuildingState{" +
                "buildingId=" + buildingId +
                ", buildingType=" + buildingType +
                ", buildingLv=" + buildingLv +
                ", foundationId=" + foundationId +
                ", residentTopLimit=" + residentTopLimit +
                ", residentCnt=" + residentCnt +
                ", heroIds=" + heroIds +
                ", economicCropData=" + economicCropData +
                ", curProductCrop=" + curProductCrop +
                ", landType=" + landType +
                '}';
    }
}
