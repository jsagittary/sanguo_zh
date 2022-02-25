package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

/**
 * 赛季英雄神职升级
 * @Description
 * @Author zhangdh
 * @Date 2021-04-13 14:51
 */
public class StaticHeroClergy {
    //唯一ID
    private int id;
    //英雄ID
    private int heroId;
    //神职阶数
    private int stage;
    //神职等级
    private int level;
    //升级对应的消耗
    private List<List<Integer>> cost;
    //该神职对应的属性
    private Map<Integer, Integer> attr;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public List<List<Integer>> getCost() {
        return cost;
    }

    public void setCost(List<List<Integer>> cost) {
        this.cost = cost;
    }

    public Map<Integer, Integer> getAttr() {
        return attr;
    }

    public void setAttr(Map<Integer, Integer> attr) {
        this.attr = attr;
    }
}
