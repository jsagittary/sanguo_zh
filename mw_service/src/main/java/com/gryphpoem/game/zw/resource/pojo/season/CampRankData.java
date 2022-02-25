package com.gryphpoem.game.zw.resource.pojo.season;

/**
 * @author xwind
 * @date 2021/5/10
 */
public class CampRankData {
    public int camp;
    public int value;
    public int time;
    public int rank;

    public CampRankData(){}

    public CampRankData(int camp, int value, int time, int rank) {
        this.camp = camp;
        this.value = value;
        this.time = time;
        this.rank = rank;
    }
}
