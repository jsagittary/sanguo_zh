package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author xwind
 * @date 2021/7/23
 */
public class StaticRandomLibrary {
    private int id;
    private int randomId;
    private List<Integer> lv;
    private List<List<Integer>> awardList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRandomId() {
        return randomId;
    }

    public void setRandomId(int randomId) {
        this.randomId = randomId;
    }

    public List<Integer> getLv() {
        return lv;
    }

    public void setLv(List<Integer> lv) {
        this.lv = lv;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }
}
