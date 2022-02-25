package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * s_fish_shop
 */
public class StaticFishShop {
    private int id;
    private List<List<Integer>> awardList;
    private List<List<Integer>> expendProp;
    private int sort;
    private int limit;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public List<List<Integer>> getExpendProp() {
        return expendProp;
    }

    public void setExpendProp(List<List<Integer>> expendProp) {
        this.expendProp = expendProp;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
