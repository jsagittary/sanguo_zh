package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 化工厂
 * 
 * @author tyler
 *
 */
public class StaticChemical {
    private int id;
    private List<List<Integer>> cost;
    private List<List<Integer>> costItem;
    private int reward;
    private int time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<List<Integer>> getCost() {
        return cost;
    }

    public void setCost(List<List<Integer>> cost) {
        this.cost = cost;
    }

    public List<List<Integer>> getCostItem() {
        return costItem;
    }

    public void setCostItem(List<List<Integer>> costItem) {
        this.costItem = costItem;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "StaticChemical [id=" + id + ", cost=" + cost + ", costItem=" + costItem + ", reward=" + reward
                + ", time=" + time + "]";
    }

}
