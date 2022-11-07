package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 主城地基
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/5 16:02
 */
public class StaticHomeCityFoundation {

    private Integer id; // 地基id

    private List<Integer> cellList; // 地基占的格子id

    private List<Integer> buildType; // 地基可建造的建筑类型

    private Integer foundationType; // 地基类型：1：城内2：城外

    private List<List<Integer>> buffConfig; // 地貌buff及对应生效的资源建筑类型

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public Integer getFoundationType() {
        return foundationType;
    }

    public void setFoundationType(Integer foundationType) {
        this.foundationType = foundationType;
    }

    public List<List<Integer>> getBuffConfig() {
        return buffConfig;
    }

    public void setBuffConfig(List<List<Integer>> buffConfig) {
        this.buffConfig = buffConfig;
    }

}
