package com.gryphpoem.game.zw.resource.pojo.buildHomeCity;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 被委任的武将id, key-位置索引, value-武将id
     */
    private Map<Integer, Integer> heroIdInfo = new HashMap<>(2);

    /**
     * 被分配产出的经济作物id, key-位置索引, value-作物id
     */
    private Map<Integer, Integer> cropIdInfo = new HashMap<>(3);

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

    public Map<Integer, Integer> getHeroIdInfo() {
        return heroIdInfo;
    }

    public void setHeroIdInfo(Map<Integer, Integer> heroIdInfo) {
        this.heroIdInfo = heroIdInfo;
    }

    public Map<Integer, Integer> getCropIdInfo() {
        return cropIdInfo;
    }

    public void setCropIdInfo(Map<Integer, Integer> cropIdInfo) {
        this.cropIdInfo = cropIdInfo;
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

    public BuildingState dser(SerializePb.SerBuildingState ser) {
        this.buildingId = ser.getBuildingId();
        this.buildingLv = ser.getBuildingLv();
        this.buildingType = ser.getBuildingType();
        this.foundationId = ser.getFoundationId();
        this.residentTopLimit = ser.getResidentTopLimit();
        this.residentCnt = ser.getResidentCnt();
        if (CheckNull.nonEmpty(ser.getHeroIdInfoList())) {
            ser.getHeroIdInfoList().forEach(tmp -> {
                if (!this.heroIdInfo.containsKey(tmp.getV1())) {
                    this.heroIdInfo.put(tmp.getV1(), tmp.getV2());
                }
            });
        }
        if (CheckNull.nonEmpty(ser.getCropIdInfoList())) {
            ser.getCropIdInfoList().forEach(tmp -> {
                if (!this.cropIdInfo.containsKey(tmp.getV1())) {
                    this.cropIdInfo.put(tmp.getV1(), tmp.getV2());
                }
            });
        }
        if (ser.hasCurProductCropInfo()) {
            CommonPb.CurProductCropInfo curProductCropInfo = ser.getCurProductCropInfo();
            this.curProductCrop.add(curProductCropInfo.getCropId());
            this.curProductCrop.add(curProductCropInfo.getStartTime());
            this.curProductCrop.add(curProductCropInfo.getEndTime());
        }
        return this;
    }

    public SerializePb.SerBuildingState ser() {
        SerializePb.SerBuildingState.Builder ser = SerializePb.SerBuildingState.newBuilder();
        ser.setBuildingId(this.buildingId);
        ser.setBuildingLv(this.buildingLv);
        ser.setBuildingType(this.buildingType);
        ser.setFoundationId(this.foundationId);
        ser.setResidentTopLimit(this.residentTopLimit);
        ser.setResidentCnt(this.residentCnt);
        if (CheckNull.nonEmpty(this.heroIdInfo)) {
            ser.addAllHeroIdInfo(PbHelper.createTwoIntListByMap(this.heroIdInfo));
        }
        if (CheckNull.nonEmpty(this.cropIdInfo)) {
            ser.addAllCropIdInfo(PbHelper.createTwoIntListByMap(this.cropIdInfo));
        }
        if (CheckNull.nonEmpty(this.curProductCrop) && this.curProductCrop.size() >= 3) {
            CommonPb.CurProductCropInfo.Builder curProductCropInfoBuilder = CommonPb.CurProductCropInfo.newBuilder();
            curProductCropInfoBuilder.setCropId(this.curProductCrop.get(0));
            curProductCropInfoBuilder.setStartTime(this.curProductCrop.get(1));
            curProductCropInfoBuilder.setEndTime(this.curProductCrop.get(2));
            ser.setCurProductCropInfo(curProductCropInfoBuilder.build());
        }

        return ser.build();
    }

    public CommonPb.BuildingState creatPb() {
        CommonPb.BuildingState.Builder pb = CommonPb.BuildingState.newBuilder();
        pb.setBuildingId(this.getBuildingId());
        pb.setBuildingLv(this.buildingLv);
        pb.setBuildingType(this.buildingType);
        pb.setFoundationId(this.foundationId);
        pb.setResidentTopLimit(this.residentTopLimit);
        pb.setResidentCnt(this.residentCnt);
        if (CheckNull.nonEmpty(this.heroIdInfo)) {
            pb.addAllHeroIdInfo(PbHelper.createTwoIntListByMap(this.heroIdInfo));
        }
        pb.addAllCropId(this.cropIdInfo.values());
        if (CheckNull.nonEmpty(this.curProductCrop) && this.curProductCrop.size() >= 3) {
            CommonPb.CurProductCropInfo.Builder economicCropInfo = CommonPb.CurProductCropInfo.newBuilder();
            economicCropInfo.setCropId(this.curProductCrop.get(0));
            economicCropInfo.setStartTime(this.curProductCrop.get(1));
            economicCropInfo.setEndTime(this.curProductCrop.get(2));
            pb.setCurProductCropInfo(economicCropInfo.build());
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
                ", heroIdInfo=" + heroIdInfo +
                ", cropIdInfo=" + cropIdInfo +
                ", curProductCrop=" + curProductCrop +
                ", landType=" + landType +
                '}';
    }
}
