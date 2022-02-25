package com.gryphpoem.game.zw.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.mgr.CrossRankMgr;
import com.gryphpoem.game.zw.mgr.PlayerMgr;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.model.rank.CamRankItem;
import com.gryphpoem.game.zw.model.rank.PersonalRankItem;
import com.gryphpoem.game.zw.pb.CommonPb.CrossRankItem;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb5.*;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossWarRank;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.util.PbMsgUtil;
import com.gryphpoem.game.zw.util.RankLinkedList;

/**
 * @ClassName CrossRankService.java
 * @Description 跨服排行
 * @author QiuKun
 * @date 2019年5月29日
 */
@Component
public class CrossRankService {

    @Autowired
    private PlayerMgr playerMgr;
    @Autowired
    private CrossRankMgr crossRankMgr;

    /**
     * 获取跨服排行
     * 
     * @param crossPlayer
     * @param req
     */
    public void getCrossRank(CrossPlayer crossPlayer, GetCrossRankRq req) {
        switch (req.getType()) {
            case StaticCrossWarRank.RANK_TYPE_CAMP:
                campRankList(crossPlayer, req);
                break;
            case StaticCrossWarRank.RANK_TYPE_PERSONAL:
                personalRankList(crossPlayer, req);
                break;
            default:
                break;
        }
    }

    private void personalRankList(CrossPlayer crossPlayer, GetCrossRankRq req) {
        RankLinkedList<PersonalRankItem> rank = crossRankMgr.getPersonalRank();
        List<PersonalRankItem> rankList = null;
        int ranking = 0;
        if (req.hasPageParam()) {
            TwoInt pageParam = req.getPageParam();
            rankList = rank.getRankListLimit(pageParam.getV1(), pageParam.getV2());
            ranking = pageParam.getV1();
        } else {
            rankList = rank.getRankList();
            ranking = 0;
        }

        GetCrossRankRs.Builder builder = GetCrossRankRs.newBuilder();
        if (!CheckNull.isEmpty(rankList)) {
            for (PersonalRankItem it : rankList) {
                ranking++;
                CrossPlayer player = playerMgr.getPlayer(it.getLordId());
                if (player == null) {
                    rank.remove(it);
                    continue;
                }
                CrossRankItem.Builder b = CrossRankItem.newBuilder();
                b.setMainServerId(player.getMainServerId());
                b.setCamp(player.getCamp());
                b.setNick(player.getLordModel().getNick());
                b.setRoleId(player.getLordId());
                b.setVal(it.getVal());
                b.setType(StaticCrossWarRank.RANK_TYPE_PERSONAL);
                b.setPortrait(player.getLordModel().getPortrait());
                b.setRanking(ranking);
                builder.addItem(b.build());
            }
        }
        builder.setSize(rank.size());
        PbMsgUtil.sendOkMsgToPlayer(crossPlayer, GetCrossRankRs.EXT_FIELD_NUMBER, GetCrossRankRs.ext, builder.build());
    }

    private void campRankList(CrossPlayer crossPlayer, GetCrossRankRq req) {
        RankLinkedList<CamRankItem> rank = crossRankMgr.getCampRank();
        List<CamRankItem> rankList = null;
        int ranking = 0;
        if (req.hasPageParam()) {
            TwoInt pageParam = req.getPageParam();
            rankList = rank.getRankListLimit(pageParam.getV1(), pageParam.getV2());
            ranking = pageParam.getV1();
        } else {
            rankList = rank.getRankList();
            ranking = 0;
        }
        GetCrossRankRs.Builder builder = GetCrossRankRs.newBuilder();
        if (!CheckNull.isEmpty(rankList)) {
            for (CamRankItem it : rankList) {
                ranking++;
                CrossRankItem.Builder b = CrossRankItem.newBuilder();
                b.setMainServerId(it.getMainServerId());
                b.setCamp(it.getCamp());
                b.setVal(it.getVal());
                b.setType(StaticCrossWarRank.RANK_TYPE_CAMP);
                b.setRanking(ranking);
                builder.addItem(b.build());
            }
        }
        builder.setSize(rank.size());
        PbMsgUtil.sendOkMsgToPlayer(crossPlayer, GetCrossRankRs.EXT_FIELD_NUMBER, GetCrossRankRs.ext, builder.build());
    }
}
