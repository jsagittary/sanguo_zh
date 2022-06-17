package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

public class StaticTreasureWareLevel {
    /**
     * 主键id
     */
    private int id;
    /**
     * 品质
     */
    private int quality;
    /**
     * 等级
     */
    private int level;
    /**
     * 消耗
     */
    private List<List<Integer>> consume;
    /**
     * 属性
     */
    private Map<Integer, Integer> attr;
    /**
     * 分解材料
     */
    private List<List<Integer>> resolve;
    /** 需要的等级 */
    private int needLevel;

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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<List<Integer>> getConsume() {
        return consume;
    }

    public void setConsume(List<List<Integer>> consume) {
        this.consume = consume;
    }

    public List<List<Integer>> getResolve() {
        return resolve;
    }

    public void setResolve(List<List<Integer>> resolve) {
        this.resolve = resolve;
    }

    public Map<Integer, Integer> getAttr() {
        return attr;
    }

    public void setAttr(Map<Integer, Integer> attr) {
        this.attr = attr;
    }

    public int getNeedLevel() {
        return needLevel;
    }

    public void setNeedLevel(int needLevel) {
        this.needLevel = needLevel;
    }
}
