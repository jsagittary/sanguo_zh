package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.CommonPb;

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
    private Integer buildingId;

    /**
     * 被分配的农民数量
     */
    private Integer farmerCnt;

    /**
     * 被委任的武将id
     */
    private List<Integer> heroIds;

    /**
     * 建筑被放置的地基id
     */
    private Integer foundationId;

    public Integer getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Integer buildingId) {
        this.buildingId = buildingId;
    }

    public Integer getFarmerCnt() {
        return farmerCnt;
    }

    public void setFarmerCnt(Integer farmerCnt) {
        this.farmerCnt = farmerCnt;
    }

    public List<Integer> getHeroIds() {
        return heroIds;
    }

    public void setHeroIds(List<Integer> heroIds) {
        this.heroIds = heroIds;
    }

    public Integer getFoundationId() {
        return foundationId;
    }

    public void setFoundationId(Integer foundationId) {
        this.foundationId = foundationId;
    }

    public BuildingState() {
    }

    public BuildingState(Integer buildingId, Integer foundationId) {
        this.buildingId = buildingId;
        this.foundationId = foundationId;
        this.farmerCnt = 0;
        this.heroIds = new ArrayList<>();
    }

    public BuildingState(CommonPb.BuildingState pb) {
        this.buildingId = pb.getBuildingId();
        this.farmerCnt = pb.getFarmerCnt();
        this.heroIds = pb.getHeroIdList();
        this.foundationId = pb.getFoundationId();
    }

    public CommonPb.BuildingState creatPb() {
        CommonPb.BuildingState.Builder pb = CommonPb.BuildingState.newBuilder();
        pb.setBuildingId(this.getBuildingId());
        pb.setFarmerCnt(this.getFarmerCnt());
        pb.addAllHeroId(this.getHeroIds());
        pb.setFoundationId(this.getFoundationId());
        return pb.build();
    }
}
