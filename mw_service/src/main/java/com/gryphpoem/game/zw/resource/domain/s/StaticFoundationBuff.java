package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 地貌buff
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/18 14:19
 */
public class StaticFoundationBuff {

    private int id;

    private int landType;

    private List<Integer> buildType;

    private int addOrSub; // 1-加成；0-减少

    private int coefficient; // 影响系数，万分比

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLandType() {
        return landType;
    }

    public void setLandType(int landType) {
        this.landType = landType;
    }

    public List<Integer> getBuildType() {
        return buildType;
    }

    public void setBuildType(List<Integer> buildType) {
        this.buildType = buildType;
    }

    public int getAddOrSub() {
        return addOrSub;
    }

    public void setAddOrSub(int addOrSub) {
        this.addOrSub = addOrSub;
    }

    public int getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(int coefficient) {
        this.coefficient = coefficient;
    }
}
