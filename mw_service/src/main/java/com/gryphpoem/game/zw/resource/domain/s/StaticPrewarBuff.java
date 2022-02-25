package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 柏林会战战前buff
 * @author: ZhouJie
 * @date: Create in 2018-08-30 16:40
 * @description:
 * @modified By:
 */
public class StaticPrewarBuff {

    private int id;
    private int type;                           // buff类型
    private int level;                          // buff等级
    private List<List<Integer>> resourceCost;   // 资源消耗
    private int probalility;                    // 可获得概率
    private List<Integer> need;                 // 条件
    private int goldCost;                       // 金币消耗
    private int effect;                         // 增益效果
    private int schedule;                       // 世界进程


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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<List<Integer>> getResourceCost() {
        return resourceCost;
    }

    public void setResourceCost(List<List<Integer>> resourceCost) {
        this.resourceCost = resourceCost;
    }

    public int getProbalility() {
        return probalility;
    }

    public void setProbalility(int probalility) {
        this.probalility = probalility;
    }

    public List<Integer> getNeed() {
        return need;
    }

    public void setNeed(List<Integer> need) {
        this.need = need;
    }

    public int getGoldCost() {
        return goldCost;
    }

    public void setGoldCost(int goldCost) {
        this.goldCost = goldCost;
    }

    public int getEffect() {
        return effect;
    }

    public void setEffect(int effect) {
        this.effect = effect;
    }

    public int getSchedule() {
        return schedule;
    }

    public void setSchedule(int schedule) {
        this.schedule = schedule;
    }
}
