package com.gryphpoem.game.zw.resource.domain.s;

public class StaticTreasureWareProfile {
    /**
     * 主键id
     */
    private int id;
    /**
     * 品质
     */
    private int quality;
    /**
     * 类型 1普通 2远古
     */
    private int type;
    /**
     * 属性类型
     */
    private int attrType;
    /**
     * 专属属性id
     */
    private int specialId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getAttrType() {
        return attrType;
    }

    public void setAttrType(int attrType) {
        this.attrType = attrType;
    }

    public int getSpecialId() {
        return specialId;
    }

    public void setSpecialId(int specialId) {
        this.specialId = specialId;
    }
}
