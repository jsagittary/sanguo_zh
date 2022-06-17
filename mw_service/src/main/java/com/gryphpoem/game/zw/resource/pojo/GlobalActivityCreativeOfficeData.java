package com.gryphpoem.game.zw.resource.pojo;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.core.rank.RankItem;
import com.gryphpoem.game.zw.core.rank.SimpleRank4SkipSet;
import com.gryphpoem.game.zw.core.rank.SimpleRankComparatorFactory;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.GlobalActivity;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 玛雅音乐节创作室
 *
 * @Description
 * @Author zhangdh
 * @Date 2021-10-27 21:31
 */
public class GlobalActivityCreativeOfficeData extends GlobalActivityData {
    //KEY:阵营ID, VALUE:该阵营中玩家排名列表
    private Map<Integer, SimpleRank4SkipSet<Integer>> playerRanks = new HashMap<>();
    private SimpleRank4SkipSet<Long> campRank;

    public GlobalActivityCreativeOfficeData(GlobalActivity ser) throws InvalidProtocolBufferException {
        super(ser);
        try {
            emptyRanks();
            if (Objects.nonNull(ser.getParams())) {

                SerializePb.DbActivity actPb = SerializePb.DbActivity.parseFrom(ser.getParams());
                if (CheckNull.nonEmpty(actPb.getSaveList())) {
                    for (CommonPb.TwoInt twoInt : actPb.getSaveList()) {
                        getSaveMap().put(twoInt.getV1(), twoInt.getV2());
                    }
                }

            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    public GlobalActivityCreativeOfficeData(ActivityBase activityBase, int begin) {
        super(activityBase, begin);
        emptyRanks();
    }

    private void emptyRanks() {
        //阵营积分排名
        Comparator<RankItem<Long>> c = SimpleRankComparatorFactory.createDescComparable();
        campRank = new SimpleRank4SkipSet<>(Constant.INT_HUNDRED, c);
        for (int camp : Constant.Camp.camps) {
            campRank.update(new RankItem<>(camp, 0L));
        }
        //阵营中玩家积分排名
        Comparator<RankItem<Integer>> campComparator = SimpleRankComparatorFactory.createDescComparable();
        for (int camp : Constant.Camp.camps) {
            playerRanks.put(camp, new SimpleRank4SkipSet<>(Constant.INT_HUNDRED, campComparator));
        }
    }

    @Override
    public boolean isReset(int begin, Player player) {
        boolean isReset = super.isReset(begin, player);
        if (isReset) emptyRanks();
        return isReset;
    }

    @Override
    public GlobalActivity copyData() {
        GlobalActivity serGlobal = super.copyData();
        SerializePb.DbActivity.Builder serAct = SerializePb.DbActivity.newBuilder();
        for (Map.Entry<Integer, Integer> entry : getSaveMap().entrySet()) {
            CommonPb.TwoInt twoInt = PbHelper.createTwoIntPb(entry.getKey(), entry.getValue());
            serAct.addSave(twoInt);
        }
        serAct.setActivityId(getActivityId());
        serAct.setActKeyId(getActivityKeyId());
        serGlobal.setParams(serAct.build().toByteArray());
        return serGlobal;
    }

    public void updatePlayerRank(int camp, long lordId, int score, long nowTime) {
        RankItem<Integer> rit = new RankItem<>(lordId, score, nowTime);
        SimpleRank4SkipSet<Integer> ranks = playerRanks.get(camp);
        ranks.update(rit);
    }

    public void updateCampRank(int camp, long campScore, long nowTime) {
        RankItem<Long> rit = new RankItem<>(camp, campScore, nowTime);
        campRank.update(rit);
    }

    public SimpleRank4SkipSet<Long> getCampRank() {
        return campRank;
    }

    public SimpleRank4SkipSet<Integer> getPersonRank(int camp) {
        return playerRanks.get(camp);
    }
}
