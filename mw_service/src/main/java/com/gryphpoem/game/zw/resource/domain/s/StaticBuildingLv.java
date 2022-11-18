package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticBuildingLv.java
 * @Description 建筑等级配置
 * @author TanDonghai
 * @date 创建时间：2017年3月11日 上午11:00:09
 *
 */
public class StaticBuildingLv {
    private int id;
    private int buildingType;// 建筑类型
    private int level;// 建筑级别
    private int roleLv;// 升级所需玩家等级
    private int upTime;// 升到本级需要的升级时间（秒）
    private List<Integer> upNeedBuilding;// 升至本级需要的建筑相关
    private List<List<Integer>> upNeedSkill;// 升至本级需要的技能相关
    private List<List<Integer>> upNeedResource;// 升至本级需要的资源
    private List<Integer> resourceOut;// 本级一次征收的资源产量
    private List<List<Integer>> capacity;// 该级的容量或资源的每小时产出
    private List<List<Integer>> fixCost;// 修复消耗
    private int resident; // 等级对应的居民上限

    public int getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(int buildingType) {
        this.buildingType = buildingType;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getRoleLv() {
        return roleLv;
    }

    public void setRoleLv(int roleLv) {
        this.roleLv = roleLv;
    }

    public List<Integer> getUpNeedBuilding() {
        return upNeedBuilding;
    }

    public void setUpNeedBuilding(List<Integer> upNeedBuilding) {
        this.upNeedBuilding = upNeedBuilding;
    }

    public List<List<Integer>> getUpNeedSkill() {
        return upNeedSkill;
    }

    public void setUpNeedSkill(List<List<Integer>> upNeedSkill) {
        this.upNeedSkill = upNeedSkill;
    }

    public List<List<Integer>> getUpNeedResource() {
        return upNeedResource;
    }

    public void setUpNeedResource(List<List<Integer>> upNeedResource) {
        this.upNeedResource = upNeedResource;
    }

    public List<Integer> getResourceOut() {
        return resourceOut;
    }

    public void setResourceOut(List<Integer> resourceOut) {
        this.resourceOut = resourceOut;
    }

    public List<List<Integer>> getCapacity() {
        return capacity;
    }

    public void setCapacity(List<List<Integer>> capacity) {
        this.capacity = capacity;
    }

    public int getUpTime() {
        return upTime;
    }

    public void setUpTime(int upTime) {
        this.upTime = upTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<List<Integer>> getFixCost() {
        return fixCost;
    }

    public void setFixCost(List<List<Integer>> fixCost) {
        this.fixCost = fixCost;
    }

    public int getResident() {
        return resident;
    }

    public void setResident(int resident) {
        this.resident = resident;
    }
}
