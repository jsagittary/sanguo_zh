package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticGuideBuild.java
 * @Description 新手指引建筑部分的步骤配置
 * @author QiuKun
 * @date 2017年10月31日
 */
public class StaticGuideBuild {
    private int buildingId; // 建筑id
    private int buildUp;// 建筑升级时给的步骤id
    private int buildFinlish;// 建筑升级完成的步骤id

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public int getBuildUp() {
        return buildUp;
    }

    public void setBuildUp(int buildUp) {
        this.buildUp = buildUp;
    }

    public int getBuildFinlish() {
        return buildFinlish;
    }

    public void setBuildFinlish(int buildFinlish) {
        this.buildFinlish = buildFinlish;
    }

}
