package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 英雄组合表 s_fish_bait_herocombination
 *
 * @author xwind
 * @date 2021/8/6
 */
public class StaticFishBaitHerocombination {
    private int id;
    private int priority;
    private List<List<Integer>> personnel;
    private List<Integer> heroLV;
    private List<List<Integer>> collectionResult;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<List<Integer>> getCollectionResult() {
        return collectionResult;
    }

    public void setCollectionResult(List<List<Integer>> collectionResult) {
        this.collectionResult = collectionResult;
    }

    public List<List<Integer>> getPersonnel() {
        return personnel;
    }

    public void setPersonnel(List<List<Integer>> personnel) {
        this.personnel = personnel;
    }

    public List<Integer> getHeroLV() {
        return heroLV;
    }

    public void setHeroLV(List<Integer> heroLV) {
        this.heroLV = heroLV;
    }
}
