package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author xwind
 * @date 2022/8/4
 */
public class StaticRelicShop {
    private int id;
    private int sort;
    private List<List<Integer>> award;
    private List<List<Integer>> partyAward;
    private int price;
    private List<Integer> area;

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

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public List<List<Integer>> getPartyAward() {
        return partyAward;
    }

    public void setPartyAward(List<List<Integer>> partyAward) {
        this.partyAward = partyAward;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public List<Integer> getArea() {
        return area;
    }

    public void setArea(List<Integer> area) {
        this.area = area;
    }
}
