package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-27 16:37
 * @description: 教官技能配置
 * @modified By:
 */
public class StaticMentorSkill {

    private int id;                             // id
    private int type;                           // 类型
    private int lv;                             // 等级
    private Map<Integer, Integer> attr;         // 加成属性
    private List<List<Integer>> cost;           // 消耗的资源
    private List<List<Integer>> activeItem;     // 激活技能消耗
    private Map<Integer, Integer> skill;        // 专业技能
    private int mentorType;                     // 教官类型
    private int armyType;                       // 起效的兵种类型 1 坦克 2 战车 3 火箭

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

    public Map<Integer, Integer> getAttr() {
        return attr;
    }

    public void setAttr(Map<Integer, Integer> attr) {
        this.attr = attr;
    }

    public List<List<Integer>> getCost() {
        return cost;
    }

    public void setCost(List<List<Integer>> cost) {
        this.cost = cost;
    }

    public List<List<Integer>> getActiveItem() {
        return activeItem;
    }

    public void setActiveItem(List<List<Integer>> activeItem) {
        this.activeItem = activeItem;
    }

    public Map<Integer, Integer> getSkill() {
        return skill;
    }

    public void setSkill(Map<Integer, Integer> skill) {
        this.skill = skill;
    }

    public int getMentorType() {
        return mentorType;
    }

    public void setMentorType(int mentorType) {
        this.mentorType = mentorType;
    }

    public int getArmyType() {
        return armyType;
    }

    public void setArmyType(int armyType) {
        this.armyType = armyType;
    }
}
