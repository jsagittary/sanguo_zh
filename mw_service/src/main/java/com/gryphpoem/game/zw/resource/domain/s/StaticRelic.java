package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author xwind
 * @date 2022/8/3
 */
public class StaticRelic {
    private int id;
    private List<Integer> era;
    private List<List<Integer>> area;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getEra() {
        return era;
    }

    public void setEra(List<Integer> era) {
        this.era = era;
    }

    public List<List<Integer>> getArea() {
        return area;
    }

    public void setArea(List<List<Integer>> area) {
        this.area = area;
    }
}
