package com.gryphpoem.game.zw.resource.domain.s;

/**
 * 钓鱼结果表 s_fish_results
 *
 * @author xwind
 * @date 2021/8/6
 */
public class StaticFishResults {
    private int colorID;
    private int fishingHaverest;
    private int getproficiency;

    public int getColorID() {
        return colorID;
    }

    public void setColorID(int colorID) {
        this.colorID = colorID;
    }

    public int getFishingHaverest() {
        return fishingHaverest;
    }

    public void setFishingHaverest(int fishingHaverest) {
        this.fishingHaverest = fishingHaverest;
    }

    public int getGetproficiency() {
        return getproficiency;
    }

    public void setGetproficiency(int getproficiency) {
        this.getproficiency = getproficiency;
    }
}
