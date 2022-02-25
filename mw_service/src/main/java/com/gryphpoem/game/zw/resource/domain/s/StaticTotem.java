package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author xwind
 * @date 2021/11/18
 */
public class StaticTotem {
    private int id;
    private int quality;
    private int place;
    private int armType;
    private int breakId;
    private List<List<Integer>> beakNeed;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getBreakId() {
        return breakId;
    }

    public void setBreakId(int breakId) {
        this.breakId = breakId;
    }

    public List<List<Integer>> getBeakNeed() {
        return beakNeed;
    }

    public void setBeakNeed(List<List<Integer>> beakNeed) {
        this.beakNeed = beakNeed;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public int getArmType() {
        return armType;
    }

    public void setArmType(int armType) {
        this.armType = armType;
    }
}
