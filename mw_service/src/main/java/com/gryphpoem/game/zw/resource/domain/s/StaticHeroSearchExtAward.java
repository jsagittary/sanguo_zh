package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @description:
 * @author: zhou jie
 * @time: 2022/2/28 17:49
 */
public class StaticHeroSearchExtAward {

    private int id;
    private int searchType;
    private int searchCount;
    private List<List<Integer>> searchAward;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSearchType() {
        return searchType;
    }

    public void setSearchType(int searchType) {
        this.searchType = searchType;
    }

    public int getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(int searchCount) {
        this.searchCount = searchCount;
    }

    public List<List<Integer>> getSearchAward() {
        return searchAward;
    }

    public void setSearchAward(List<List<Integer>> searchAward) {
        this.searchAward = searchAward;
    }
}
