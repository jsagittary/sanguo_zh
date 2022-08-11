package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.Calendar;
import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-13 18:40
 */
public class StaticDrawCardWeight {
    private int id;
    private int searchTypeId;
    private List<List<Integer>> weight;
    private List<List<Integer>> serverIdList;
    private int totalWeight;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSearchTypeId() {
        return searchTypeId;
    }

    public void setSearchTypeId(int searchTypeId) {
        this.searchTypeId = searchTypeId;
    }

    public List<List<Integer>> getWeight() {
        return weight;
    }

    public void setWeight(List<List<Integer>> weight) {
        this.weight = weight;
    }

    public List<List<Integer>> getServerIdList() {
        return serverIdList;
    }

    public void setServerIdList(List<List<Integer>> serverIdList) {
        this.serverIdList = serverIdList;
    }

    public int getTotalWeight() {
        if (this.totalWeight == 0)
            initTotalWeight();
        return totalWeight;
    }

    public void setTotalWeight(int totalWeight) {
        this.totalWeight = totalWeight;
    }

    public StaticDrawCardWeight initTotalWeight() {
        if (CheckNull.isEmpty(weight))
            return this;
        this.totalWeight = 0;
        weight.forEach(list -> totalWeight += list.get(1));
        return this;
    }
}
