package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 垂钓熟练度表 s_fish_proficiency
 *
 * @author xwind
 * @date 2021/8/6
 */
public class StaticFishProficiency {
    private int titleID;
    private int exp;
    private int reduceSpeed;
    private List<Integer> multipleCrit;
    private List<List<Integer>> sizeUP;
    private List<List<Integer>> goalUP;

    public int getTitleID() {
        return titleID;
    }

    public void setTitleID(int titleID) {
        this.titleID = titleID;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getReduceSpeed() {
        return reduceSpeed;
    }

    public void setReduceSpeed(int reduceSpeed) {
        this.reduceSpeed = reduceSpeed;
    }

    public List<Integer> getMultipleCrit() {
        return multipleCrit;
    }

    public void setMultipleCrit(List<Integer> multipleCrit) {
        this.multipleCrit = multipleCrit;
    }

    public List<List<Integer>> getSizeUP() {
        return sizeUP;
    }

    public void setSizeUP(List<List<Integer>> sizeUP) {
        this.sizeUP = sizeUP;
    }

    public List<List<Integer>> getGoalUP() {
        return goalUP;
    }

    public void setGoalUP(List<List<Integer>> goalUP) {
        this.goalUP = goalUP;
    }
}
