package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-27 11:30
 * @description: 教官装备配置
 * @modified By:
 */
public class StaticMentorEquip {

    private int id;                       // id
    private int type;                     // 装备类型
    private int order;                    // 装备阶级
    private int gearOrder;                // 装备阶级Id
    private int gearLevel;                // 装备星级
    private int attack;                   // 装备加成攻击
    private int defenseLow;               // 装备随机加成防御的下限
    private int defenseHigh;              // 装备加成防御的上限
    private int gearWeight;               // 随机权重
    private int mentorType;               // 教官类型
    private List<Integer> vendorPrice;    // 装备回收所得钞票数量

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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getGearOrder() {
        return gearOrder;
    }

    public void setGearOrder(int gearOrder) {
        this.gearOrder = gearOrder;
    }

    public int getGearLevel() {
        return gearLevel;
    }

    public void setGearLevel(int gearLevel) {
        this.gearLevel = gearLevel;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefenseLow() {
        return defenseLow;
    }

    public void setDefenseLow(int defenseLow) {
        this.defenseLow = defenseLow;
    }

    public int getDefenseHigh() {
        return defenseHigh;
    }

    public void setDefenseHigh(int defenseHigh) {
        this.defenseHigh = defenseHigh;
    }

    public int getGearWeight() {
        return gearWeight;
    }

    public void setGearWeight(int gearWeight) {
        this.gearWeight = gearWeight;
    }

    public int getMentorType() {
        return mentorType;
    }

    public void setMentorType(int mentorType) {
        this.mentorType = mentorType;
    }

    public List<Integer> getVendorPrice() {
        return vendorPrice;
    }

    public void setVendorPrice(List<Integer> vendorPrice) {
        this.vendorPrice = vendorPrice;
    }
}
