package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author TanDonghai
 * @ClassName StaticEquipExtra.java
 * @Description 装备额外属性配置表
 * @date 创建时间：2017年3月28日 下午2:02:42
 */
public class StaticEquipExtra {
    /**
     * id
     */
    private int autoId;
    /**
     * 属性id
     */
    private int arrtId;
    /**
     * 等级
     */
    private int level;
    /**
     * 品质
     */
    private int quality;
    /**
     * 属性值
     */
    private int attrValue;

    /**
     * 填equip表中的部位
     */
    private List<Integer> equipPart;

    public int getAutoId() {
        return autoId;
    }

    public void setAutoId(int autoId) {
        this.autoId = autoId;
    }

    public int getArrtId() {
        return arrtId;
    }

    public void setArrtId(int arrtId) {
        this.arrtId = arrtId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getAttrValue() {
        return attrValue;
    }

    public void setAttrValue(int attrValue) {
        this.attrValue = attrValue;
    }

    public List<Integer> getEquipPart() {
        return equipPart;
    }

    public void setEquipPart(List<Integer> equipPart) {
        this.equipPart = equipPart;
    }

    @Override public String toString() {
        return "StaticEquipExtra{" + "autoId=" + autoId + ", arrtId=" + arrtId + ", level=" + level + ", quality="
                + quality + ", attrValue=" + attrValue + ", equipPart=" + equipPart + '}';
    }
}
