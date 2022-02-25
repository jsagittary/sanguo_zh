package com.gryphpoem.game.zw.gameplay.local.world.warfire;

import com.gryphpoem.game.zw.pb.CommonPb;

import java.util.HashMap;
import java.util.Map;

/**
 * 战火燎原---阵营数据
 *
 * @Description
 * @Author zhangdh
 * @Date 2020-12-31 14:24
 */
public class CampWarFire {
    //阵营ID
    private int camp;
    //阵营积分
    private int score;

    private FinishCalc calc;

    public CampWarFire(int camp) {
        this.camp = camp;
    }

    public CampWarFire(int camp, int score) {
        this(camp);
        this.score = score;
    }

    public CommonPb.CampWarFirePb toPb() {
        CommonPb.CampWarFirePb.Builder builder = CommonPb.CampWarFirePb.newBuilder();
        builder.setCamp(camp);
        builder.setScore(score);
        return builder.build();
    }

    public static class FinishCalc {
        public Map<Integer, Integer> tyCntMap = new HashMap<>();//KEY:占领的据点类型, VALUE:占领的数量
        public int extScore;//活动结束时奖励的额外积分
        public int rank;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
