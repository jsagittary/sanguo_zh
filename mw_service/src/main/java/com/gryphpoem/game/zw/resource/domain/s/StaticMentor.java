package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-26 18:33
 * @description: 教官配置
 * @modified By:
 */
public class StaticMentor {

    private int id;                             // id
    private int type;                           // 类型
    private int lv;                             // 等级
    private int exp;                            // 升到该级需要的经验值
    private Map<Integer, Integer> attr;         // 每100经验给予属性: [[属性类型1，属性值1],[属性类型2，属性值2]...]
    private Map<Integer, Integer> attrUp;       // 该等级基础属性: [[属性类型1，属性值1],[属性类型2，属性值2]...]
    private List<List<Integer>> award;          // 升级奖励
    private List<List<Integer>> unsealSkill;    // 升级开启技能: [解锁位置, 解锁技能]

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

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public Map<Integer, Integer> getAttr() {
        return attr;
    }

    public void setAttr(Map<Integer, Integer> attr) {
        this.attr = attr;
    }

    public Map<Integer, Integer> getAttrUp() {
        return attrUp;
    }

    public void setAttrUp(Map<Integer, Integer> attrUp) {
        this.attrUp = attrUp;
    }

    public List<List<Integer>> getUnsealSkill() {
        return unsealSkill;
    }

    public void setUnsealSkill(List<List<Integer>> unsealSkill) {
        this.unsealSkill = unsealSkill;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }
}
