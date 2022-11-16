package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 主城地基
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/5 16:02
 */
public class StaticHomeCityFoundation {

    private int id; // 地基id

    private List<Integer> cellList; // 地基占的格子id

    private List<Integer> buildType; // 地基可建造的建筑类型

    private int foundationType; // 地基类型：1：城内2：城外

    private int landType; // 地貌类型, 对应s_foundation_buff表的landType

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getCellList() {
        return cellList;
    }

    public void setCellList(List<Integer> cellList) {
        this.cellList = cellList;
    }

    public List<Integer> getBuildType() {
        return buildType;
    }

    public void setBuildType(List<Integer> buildType) {
        this.buildType = buildType;
    }

    public int getFoundationType() {
        return foundationType;
    }

    public void setFoundationType(int foundationType) {
        this.foundationType = foundationType;
    }

    public int getLandType() {
        return landType;
    }

    public void setLandType(int landType) {
        this.landType = landType;
    }
}
