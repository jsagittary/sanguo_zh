package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

/**
 * @author TanDonghai
 * @ClassName StaticEquip.java
 * @Description 装备配置表
 * @date 创建时间：2017年3月28日 上午11:06:04
 */
public class StaticEquip {
    /**
     * 装备id
     */
    private int equipId;
    /**
     * 部位
     */
    private int equipPart;
    /**
     * 品质
     */
    private int quality;
    /**
     * 洗练品质
     */
    private int washQuality;
    /**
     * 属性，[[attrId,attrValue]...]
     */
    private Map<Integer, Integer> attr;
    /**
     * 出现主公等级SS
     */
    private int display;
    /**
     * 主公等级解锁
     */
    private int unlock;
    /**
     * 打造时间，单位：秒
     */
    private int buildTime;
    /**
     * 材料,[[type,id,count]...]
     */
    private List<List<Integer>> material;
    /**
     * 分解获得材料
     */
    private List<List<Integer>> decompose;
    /**
     * 分解获得材料
     */
    private List<List<Integer>> decompose2;
    /**
     * 初始化秘技配置  如果有值  则直接按这里的初始化
     */
    private List<Integer> extra;

    /**
     * 作用的兵种类型
     */
    private int armType;

    public int getEquipId() {
        return equipId;
    }

    public void setEquipId(int equipId) {
        this.equipId = equipId;
    }

    public int getEquipPart() {
        return equipPart;
    }

    public void setEquipPart(int equipPart) {
        this.equipPart = equipPart;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public Map<Integer, Integer> getAttr() {
        return attr;
    }

    public void setAttr(Map<Integer, Integer> attr) {
        this.attr = attr;
    }

    public int getDisplay() {
        return display;
    }

    public void setDisplay(int display) {
        this.display = display;
    }

    public int getUnlock() {
        return unlock;
    }

    public void setUnlock(int unlock) {
        this.unlock = unlock;
    }

    public int getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(int buildTime) {
        this.buildTime = buildTime;
    }

    public List<List<Integer>> getMaterial() {
        return material;
    }

    public void setMaterial(List<List<Integer>> material) {
        this.material = material;
    }

    public List<List<Integer>> getDecompose() {
        return decompose;
    }

    public void setDecompose(List<List<Integer>> decompose) {
        this.decompose = decompose;
    }

    public List<List<Integer>> getDecompose2() {
        return decompose2;
    }

    public void setDecompose2(List<List<Integer>> decompose2) {
        this.decompose2 = decompose2;
    }

    public int getWashQuality() {
        return washQuality;
    }

    public void setWashQuality(int washQuality) {
        this.washQuality = washQuality;
    }

    public List<Integer> getExtra() {
        return extra;
    }

    public void setExtra(List<Integer> extra) {
        this.extra = extra;
    }

    public int getArmType() {
        return armType;
    }

    public void setArmType(int armType) {
        this.armType = armType;
    }

    @Override public String toString() {
        return "StaticEquip{" + "equipId=" + equipId + ", equipPart=" + equipPart + ", quality=" + quality
                + ", washQuality=" + washQuality + ", attr=" + attr + ", display=" + display + ", unlock=" + unlock
                + ", buildTime=" + buildTime + ", material=" + material + ", decompose=" + decompose + ", decompose2="
                + decompose2 + ", extra=" + extra + ", armType=" + armType + '}';
    }

}
