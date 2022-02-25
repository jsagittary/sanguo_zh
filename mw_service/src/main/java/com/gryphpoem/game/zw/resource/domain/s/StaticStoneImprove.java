package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticStoneImprove.java
 * @Description 进阶宝石配置
 * @author QiuKun
 * @date 2018年11月10日
 */
public class StaticStoneImprove {

    private int id;// 宝石的唯一id值
    private int type; // 种类 1攻击,2防御,兵力
    private int lv;// 等级
    private List<List<Integer>> attr;// 加的属性 [[类型,数值]]
    private List<List<Integer>> attrMult;// 加的属性 [[类型,数值]]
    private int needExp;// 升到下一级需要的经验
    private List<List<Integer>> breakThrough;// 突破需要的消耗

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

    public List<List<Integer>> getAttr() {
        return attr;
    }

    public void setAttr(List<List<Integer>> attr) {
        this.attr = attr;
    }

    public List<List<Integer>> getAttrMult() {
        return attrMult;
    }

    public void setAttrMult(List<List<Integer>> attrMult) {
        this.attrMult = attrMult;
    }

    public int getNeedExp() {
        return needExp;
    }

    public void setNeedExp(int needExp) {
        this.needExp = needExp;
    }

    public List<List<Integer>> getBreakThrough() {
        return breakThrough;
    }

    public void setBreakThrough(List<List<Integer>> breakThrough) {
        this.breakThrough = breakThrough;
    }

    @Override
    public String toString() {
        return "StaticStoneImprove [id=" + id + ", type=" + type + ", lv=" + lv + ", attr=" + attr + ", attrMult="
                + attrMult + ", needExp=" + needExp + "]";
    }

}
