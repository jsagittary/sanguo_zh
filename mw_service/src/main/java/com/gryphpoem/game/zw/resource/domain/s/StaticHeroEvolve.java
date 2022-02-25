package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

/**
 * @program: zombie_trunk
 * @description: 英雄进化属性表
 * @author: zhou jie
 * @create: 2019-10-22 16:58
 */
public class StaticHeroEvolve {

    private int id;
    /**
     * 进化属性组
     */
    private int group;
    /**
     * 进化部位
     */
    private int part;
    /**
     * 进化到对应登记需消耗的物品
     */
    private List<List<Integer>> consume;
    /**
     * 进化到该级添加的属性
     */
    private Map<Integer, Integer> attr;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getPart() {
        return part;
    }

    public void setPart(int part) {
        this.part = part;
    }

    public List<List<Integer>> getConsume() {
        return consume;
    }

    public void setConsume(List<List<Integer>> consume) {
        this.consume = consume;
    }

    public Map<Integer, Integer> getAttr() {
        return attr;
    }

    public void setAttr(Map<Integer, Integer> attr) {
        this.attr = attr;
    }

    @Override
    public String toString() {
        return "StaticHeroEvolve{" +
                "id=" + id +
                ", group=" + group +
                ", part=" + part +
                ", consume=" + consume +
                ", attr=" + attr +
                '}';
    }
}