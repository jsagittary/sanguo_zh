package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticStone.java
 * @Description 宝石
 * @author QiuKun
 * @date 2018年5月7日
 */
public class StaticStone {

    private int id;// 宝石的唯一id值
    private int type; // 种类 1攻击,2防御,兵力
    private int lv;// 等级
    private List<List<Integer>> attr;// 加的属性 [[类型,数值]]
    private int needRoleLv;// 合成需要角色的等级
    private int needNumber;// 合成需要上一等级宝石的个数
    private int needLv1Number; // 等价1级宝石的数量
    private int stoneImproveId;// 是否可以进阶 1表示可进阶

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getNeedRoleLv() {
        return needRoleLv;
    }

    public void setNeedRoleLv(int needRoleLv) {
        this.needRoleLv = needRoleLv;
    }

    public int getNeedNumber() {
        return needNumber;
    }

    public void setNeedNumber(int needNumber) {
        this.needNumber = needNumber;
    }

    public List<List<Integer>> getAttr() {
        return attr;
    }

    public void setAttr(List<List<Integer>> attr) {
        this.attr = attr;
    }

    public int getNeedLv1Number() {
        return needLv1Number;
    }

    public void setNeedLv1Number(int needLv1Number) {
        this.needLv1Number = needLv1Number;
    }

    public int getStoneImproveId() {
        return stoneImproveId;
    }

    public void setStoneImproveId(int stoneImproveId) {
        this.stoneImproveId = stoneImproveId;
    }

    /**
     * 是否可以进阶
     * 
     * @return
     */
    public boolean isCanImprove() {
        return this.stoneImproveId > 0;
    }

    @Override
    public String toString() {
        return "StaticStone [id=" + id + ", type=" + type + ", lv=" + lv + ", attr=" + attr + ", needRoleLv="
                + needRoleLv + ", needNumber=" + needNumber + ", needLv1Number=" + needLv1Number + ", stoneImproveId="
                + stoneImproveId + "]";
    }

}
