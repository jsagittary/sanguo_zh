package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author xwind
 * @date 2021/11/18
 */
public class StaticTotemDrop {
    private int id;
    private int type;
    private List<Integer> range;
    private int prob;
    private List<List<Integer>> group;

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

    public List<Integer> getRange() {
        return range;
    }

    public void setRange(List<Integer> range) {
        this.range = range;
    }

    public int getProb() {
        return prob;
    }

    public void setProb(int prob) {
        this.prob = prob;
    }

    public List<List<Integer>> getGroup() {
        return group;
    }

    public void setGroup(List<List<Integer>> group) {
        this.group = group;
    }
}
