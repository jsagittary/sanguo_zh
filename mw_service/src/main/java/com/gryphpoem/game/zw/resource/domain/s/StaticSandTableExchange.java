package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

public class StaticSandTableExchange {
    private int id;
    private int sort;
    private int type;
    private int param;
    private List<List<Integer>> award;
    private List<List<Integer>> expendProp;
    private int numberLimit;
    private List<List<Integer>> seasons;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getParam() {
        return param;
    }

    public void setParam(int param) {
        this.param = param;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public List<List<Integer>> getExpendProp() {
        return expendProp;
    }

    public void setExpendProp(List<List<Integer>> expendProp) {
        this.expendProp = expendProp;
    }

    public int getNumberLimit() {
        return numberLimit;
    }

    public void setNumberLimit(int numberLimit) {
        this.numberLimit = numberLimit;
    }

    public List<List<Integer>> getSeasons() {
        return seasons;
    }

    public void setSeasons(List<List<Integer>> seasons) {
        this.seasons = seasons;
    }
}
