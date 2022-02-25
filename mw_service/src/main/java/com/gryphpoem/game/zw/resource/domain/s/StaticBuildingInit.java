package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticBuilding.java
 * @Description 建筑开启条件
 * @author TanDonghai
 * @date 创建时间：2017年3月11日 上午10:59:03
 *
 */
public class StaticBuildingInit {
    private int buildingId;//
    private int buildingType;// 建筑类型
    private List<Integer> openRequire;// 由0建造到1级需要的图纸条件
    private int initLv;// 初始等级 0->1是建造
    private int maxLv;// 最大等级
    private int canUp;// 能否升级 0.不可升级; 1.可升级
    private int canDestroy; // 能否拆除 0.不能拆除 1.可以拆除
    private List<Integer> canBuildType;// 拆除后可建筑的类型

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

    public List<Integer> getOpenRequire() {
        return openRequire;
    }

    public void setOpenRequire(List<Integer> openRequire) {
        this.openRequire = openRequire;
    }

    public int getInitLv() {
        return initLv;
    }

    public void setInitLv(int initLv) {
        this.initLv = initLv;
    }

    public int getCanUp() {
        return canUp;
    }

    public void setCanUp(int canUp) {
        this.canUp = canUp;
    }

    public int getCanDestroy() {
        return canDestroy;
    }

    public void setCanDestroy(int canDestroy) {
        this.canDestroy = canDestroy;
    }

    public List<Integer> getCanBuildType() {
        return canBuildType;
    }

    public void setCanBuildType(List<Integer> canBuildType) {
        this.canBuildType = canBuildType;
    }

    public int getMaxLv() {
        return maxLv;
    }

    public void setMaxLv(int maxLv) {
        this.maxLv = maxLv;
    }

}
