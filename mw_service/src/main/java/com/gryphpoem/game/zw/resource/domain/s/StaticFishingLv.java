package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author xwind
 * @date 2021/12/27
 */
public class StaticFishingLv {
    private int activityId;
    private int lv;
    private List<List<Integer>> fishId;
    private List<Integer> wave;
    private int upScore;

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public List<List<Integer>> getFishId() {
        return fishId;
    }

    public void setFishId(List<List<Integer>> fishId) {
        this.fishId = fishId;
    }

    public List<Integer> getWave() {
        return wave;
    }

    public void setWave(List<Integer> wave) {
        this.wave = wave;
    }

    public int getUpScore() {
        return upScore;
    }

    public void setUpScore(int upScore) {
        this.upScore = upScore;
    }
}
