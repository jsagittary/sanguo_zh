package com.gryphpoem.game.zw.resource.pojo.attr;

/**
 * 属性类目,用来挂载在装备或者类似装备这样可以提供属性加成的地方
 * 通常此属性可以支持强化, 提升品阶, 等操作...
 * Description:
 * Author: zhangdh
 * createTime: 2022-03-01 15:06
 */
public class AttrItem {
    //属性栏目所属位置
    protected int index;
    //属性ID
    protected int attrId;
    //属性值
    protected int value;
    //属性等级
    protected int level;
    //属性品质
    protected int quality;

    public AttrItem() {
    }

    public AttrItem(int attrId, int value) {
        this.attrId = attrId;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getAttrId() {
        return attrId;
    }

    public void setAttrId(int attrId) {
        this.attrId = attrId;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
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

    public String logAttr() {
        return attrId + "," + value + ";";
    }

    @Override
    public String toString() {
        return "AttrItem{" +
                "index=" + index +
                ", attrId=" + attrId +
                ", value=" + value +
                ", level=" + level +
                ", quality=" + quality +
                '}';
    }
}
