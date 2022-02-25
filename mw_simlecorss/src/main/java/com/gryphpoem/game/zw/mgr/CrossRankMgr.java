package com.gryphpoem.game.zw.mgr;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.model.global.BaseGlobalSaveModel;
import com.gryphpoem.game.zw.model.global.GlobalModelType;
import com.gryphpoem.game.zw.model.rank.CamRankItem;
import com.gryphpoem.game.zw.model.rank.PersonalRankItem;
import com.gryphpoem.game.zw.pb.CommonPb.CrossRankItem;
import com.gryphpoem.game.zw.pb.SerializePb.SerCrossRank;
import com.gryphpoem.game.zw.util.CrossWarFinlishClear;
import com.gryphpoem.game.zw.util.RankLinkedList;

/**
 * @ClassName CrossRankMgr.java
 * @Description 跨服排行
 * @author QiuKun
 * @date 2019年5月29日
 */
@Component
public class CrossRankMgr implements CrossWarFinlishClear, BaseGlobalSaveModel {

    // 阵营排行
    public final RankLinkedList<CamRankItem> campRank = new RankLinkedList<>(200);
    // 个人排行
    public final RankLinkedList<PersonalRankItem> personalRank = new RankLinkedList<>(200);

    /**
     * 添加阵营排行
     * 
     * @param mainServerId
     * @param camp
     * @param val
     */
    public void addCamRankItem(int mainServerId, int camp, int val) {
        campRank.addRankItem(o -> o.getMainServerId() == mainServerId && o.getCamp() == camp,
                () -> new CamRankItem(mainServerId, camp), val);
    }

    /**
     * 个人排行
     * 
     * @param lordId
     * @param val
     */
    public void addPersonalRankItem(long lordId, int val) {
        personalRank.addRankItem(o -> o.getLordId() == lordId, () -> new PersonalRankItem(lordId), val);
    }

    /**
     * 获取阵营排名
     * 
     * @param mainServerId
     * @param camp
     * @return
     */
    public int findCampRanking(int mainServerId, int camp) {
        return campRank.getItemRanking(o -> o.getMainServerId() == mainServerId && o.getCamp() == camp);
    }

    /**
     * 获取个人榜排名
     * 
     * @param lordId
     * @return
     */
    public int findPersonalRanking(long lordId) {
        return personalRank.getItemRanking(o -> o.getLordId() == lordId);
    }

    /**
     * 杀敌数的变化
     * 
     * @param player
     * @param val
     */
    public void killNumChange(CrossPlayer player, int val) {
        if (val <= 0) {
            return;
        }
        player.getCrossWarModel().addKillNum(val); // 玩家的杀敌数增加
        addPersonalRankItem(player.getLordId(), val);
        addCamRankItem(player.getMainServerId(), player.getCamp(), val);
    }

    public RankLinkedList<CamRankItem> getCampRank() {
        return campRank;
    }

    public RankLinkedList<PersonalRankItem> getPersonalRank() {
        return personalRank;
    }

    @Override
    public void clear() {
        campRank.clear();
        personalRank.clear();
        LogUtil.debug("排行榜数据清除");
    }

    @Override
    public byte[] getData() {
        if (campRank.isEmpty() || personalRank.isEmpty()) {
            return null;
        }
        SerCrossRank.Builder builder = SerCrossRank.newBuilder();
        List<CrossRankItem> campRankPb = campRank.getRankList().stream().map(i -> i.toCrossRankItemPb())
                .collect(Collectors.toList());
        builder.addAllCampRank(campRankPb);
        List<CrossRankItem> personalRankPB = personalRank.getRankList().stream().map(i -> i.toCrossRankItemPb())
                .collect(Collectors.toList());
        builder.addAllPersonalRank(personalRankPB);
        return builder.build().toByteArray();
    }

    @Override
    public void loadData(byte[] data) {
        if (data != null) {
            try {
                SerCrossRank pb = SerCrossRank.parseFrom(data);
                campRank.addAll(pb.getCampRankList().stream().map(CamRankItem::new).collect(Collectors.toList()));
                personalRank.addAll(
                        pb.getPersonalRankList().stream().map(PersonalRankItem::new).collect(Collectors.toList()));
            } catch (InvalidProtocolBufferException e) {
                LogUtil.error(e);
            }
        }
    }

    @Override
    public GlobalModelType getModelType() {
        return GlobalModelType.RANK_MODEL;
    }

}
