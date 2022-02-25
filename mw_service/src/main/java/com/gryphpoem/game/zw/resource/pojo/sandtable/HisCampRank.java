package com.gryphpoem.game.zw.resource.pojo.sandtable;

import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.util.ListUtils;

import java.util.ArrayList;
import java.util.List;

public class HisCampRank {

    public int hisDate;
    public List<RankInfo> hisInfos;

    public HisCampRank(){
        hisInfos = new ArrayList<>();
    }

    public static class RankInfo {
        public int rank;
        public int camp;
        public int score;
        public int flag;
        public int killed;

        public void deser(SerializePb.SerRankInfo serRankInfo){
            this.rank = serRankInfo.getRank();
            this.camp = serRankInfo.getCamp();
            this.score = serRankInfo.getScore();
            this.flag = serRankInfo.getFlag();
            this.killed = serRankInfo.getKilled();
        }
    }

    public void deser(SerializePb.SerHisCampRank serHisCampRank){
        this.hisDate = serHisCampRank.getHisDate();
        if(!ListUtils.isBlank(serHisCampRank.getHisInfoList())){
            serHisCampRank.getHisInfoList().forEach(o -> {
                RankInfo rankInfo = new RankInfo();
                rankInfo.deser(o);
                this.hisInfos.add(rankInfo);
            });
        }
    }
}
