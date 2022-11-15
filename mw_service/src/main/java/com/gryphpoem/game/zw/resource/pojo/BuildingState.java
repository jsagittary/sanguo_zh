package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.CommonPb;

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
     * 被分配的农民数量
     */
    private int residentCnt;

    /**
     * 被委任的武将id
     */
    private List<Integer> heroIds;

    /**
     * 建筑被放置的地基id
     */
    private int foundationId;

    /**
     * 被分配产出的经济作物, key-经济作物id, value-开始产出时间
     */
    private Map<Integer, Integer> economicCropData = new HashMap<>();

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

    public Map<Integer, Integer> getEconomicCropData() {
        return economicCropData;
    }

    public void setEconomicCropData(Map<Integer, Integer> economicCropData) {
        this.economicCropData = economicCropData;
    }

    public BuildingState() {
    }

    public BuildingState(Integer buildingId, Integer foundationId) {
        this.buildingId = buildingId;
        this.foundationId = foundationId;
        this.residentCnt = 0;
        this.heroIds = new ArrayList<>();
    }

    public BuildingState(CommonPb.BuildingState pb) {
        this.buildingId = pb.getBuildingId();
        this.residentCnt = pb.getResidentCnt();
        this.heroIds = pb.getHeroIdList();
        this.foundationId = pb.getFoundationId();
    }

    public CommonPb.BuildingState creatPb() {
        CommonPb.BuildingState.Builder pb = CommonPb.BuildingState.newBuilder();
        pb.setBuildingId(this.getBuildingId());
        pb.setResidentCnt(this.getResidentCnt());
        pb.addAllHeroId(this.getHeroIds());
        pb.setFoundationId(this.getFoundationId());
        return pb.build();
    }

}
