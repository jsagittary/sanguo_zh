package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 鱼属性表 s_fishattribute
 *
 * @author xwind
 * @date 2021/8/6
 */
public class StaticFishattribute {
    private int fishID;
    private int quality;
    private int basesize;
    private int basegoal;
    private List<List<Integer>> reward;
    private List<Integer> sizeChange;

    public int getFishID() {
        return fishID;
    }

    public void setFishID(int fishID) {
        this.fishID = fishID;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getBasesize() {
        return basesize;
    }

    public void setBasesize(int basesize) {
        this.basesize = basesize;
    }

    public int getBasegoal() {
        return basegoal;
    }

    public void setBasegoal(int basegoal) {
        this.basegoal = basegoal;
    }

    public List<List<Integer>> getReward() {
        return reward;
    }

    public void setReward(List<List<Integer>> reward) {
        this.reward = reward;
    }

    public List<Integer> getSizeChange() {
        return sizeChange;
    }

    public void setSizeChange(List<Integer> sizeChange) {
        this.sizeChange = sizeChange;
    }
}
