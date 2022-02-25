package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticBuilding.java
 * @Description 建筑位置能建什么
 * @author TanDonghai
 * @date 创建时间：2017年3月11日 上午10:59:03
 *
 */
@Deprecated
public class StaticBuilding {
    private int buildingId;//
    private int buildingType;// 建筑类型
    private int canDestroy;// 能否拆除 0.不能拆除 1.可以拆除

    // private int canUp;// 能否升级 0.不能升级 1.可以升级
    // private int canResource;// 是否能生产资源 0.不能生产 1.可以生产
    // private int openType;// 建筑开启条件类型，-1 默认开启，1 任务，2 关卡，3 主公等级，4 司令部等级
    // private int openLimit;// 建筑开启的限制，任务id，关卡id，玩家等级等
    // private int initLv;// 初始等级

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

    public int getCanDestroy() {
        return canDestroy;
    }

    public void setCanDestroy(int canDestroy) {
        this.canDestroy = canDestroy;
    }

    @Override
    public String toString() {
        return "StaticBuilding [buildingId=" + buildingId + ", buildingType=" + buildingType + ", canDestroy="
                + canDestroy + "]";
    }

    // public int getCanUp() {
    // return canUp;
    // }
    //
    // public void setCanUp(int canUp) {
    // this.canUp = canUp;
    // }
    //
    // public int getOpenType() {
    // return openType;
    // }
    //
    // public void setOpenType(int openType) {
    // this.openType = openType;
    // }
    //
    // public int getOpenLimit() {
    // return openLimit;
    // }
    //
    // public void setOpenLimit(int openLimit) {
    // this.openLimit = openLimit;
    // }
    //
    // public int getInitLv() {
    // return initLv;
    // }
    //
    // public void setInitLv(int initLv) {
    // this.initLv = initLv;
    // }
    
    

}
