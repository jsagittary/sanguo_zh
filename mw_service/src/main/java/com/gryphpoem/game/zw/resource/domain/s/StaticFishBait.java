package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 鱼饵表 s_fish_bait
 *
 * @author xwind
 * @date 2021/8/6
 */
public class StaticFishBait {
    private int baitID;
    private int propID;
    private int quality;
    private int gettime;
    private List<List<Integer>> fishID;
    private int basespeed;
    private int lightgreen;
    private int darkgreen;

    public int getBaitID() {
        return baitID;
    }

    public void setBaitID(int baitID) {
        this.baitID = baitID;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getGettime() {
        return gettime;
    }

    public void setGettime(int gettime) {
        this.gettime = gettime;
    }

    public List<List<Integer>> getFishID() {
        return fishID;
    }

    public void setFishID(List<List<Integer>> fishID) {
        this.fishID = fishID;
    }

    public int getBasespeed() {
        return basespeed;
    }

    public void setBasespeed(int basespeed) {
        this.basespeed = basespeed;
    }

    public int getLightgreen() {
        return lightgreen;
    }

    public void setLightgreen(int lightgreen) {
        this.lightgreen = lightgreen;
    }

    public int getDarkgreen() {
        return darkgreen;
    }

    public void setDarkgreen(int darkgreen) {
        this.darkgreen = darkgreen;
    }

    public int getPropID() {
        return propID;
    }

    public void setPropID(int propID) {
        this.propID = propID;
    }
}
