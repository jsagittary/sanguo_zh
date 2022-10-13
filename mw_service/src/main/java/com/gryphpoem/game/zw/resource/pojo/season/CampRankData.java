package com.gryphpoem.game.zw.resource.pojo.season;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @author xwind
 * @date 2021/5/10
 */
public class CampRankData {
    public int camp;
    public int value;
    public int time;
    public int rank;

    public CampRankData() {
    }

    public CampRankData(int camp, int value, int time, int rank) {
        this.camp = camp;
        this.value = value;
        this.time = time;
        this.rank = rank;
    }

    public CampRankData copyNew() {
        return new CampRankData(camp, value, time, rank);
    }

    public CommonPb.CampRankInfo ser() {
        return CommonPb.CampRankInfo.newBuilder().setCamp(camp).setValue(value).setTime(time).setRank(rank).build();
    }

    public void dser(CommonPb.CampRankInfo campRankInfo) {
        this.camp = campRankInfo.getCamp();
        this.value = campRankInfo.getValue();
        this.time = campRankInfo.getTime();
        this.rank = campRankInfo.getRank();
    }
}
