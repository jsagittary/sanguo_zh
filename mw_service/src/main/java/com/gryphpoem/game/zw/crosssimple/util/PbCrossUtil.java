package com.gryphpoem.game.zw.crosssimple.util;

import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.gryphpoem.cross.chat.CrossChatConst;
import com.gryphpoem.cross.chat.dto.CrossChat;
import com.gryphpoem.cross.chat.dto.CrossRoleChat;
import com.gryphpoem.cross.chat.dto.CrossSystemChat;
import com.gryphpoem.cross.common.RankItemInt;
import com.gryphpoem.cross.gameplay.battle.c2g.dto.MilitarySituation;
import com.gryphpoem.cross.player.dto.PlayerLordDto;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.CrossAwardPb;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.ChatConst;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName PbCrossUtil.java
 * @Description
 * @date 2019年5月13日
 */
public abstract class PbCrossUtil {

    public static List<CommonPb.CrossRankItem> buildRanks(List<RankItemInt> ranks, Map<Long, PlayerLordDto> lordMap) {
        List<CommonPb.CrossRankItem> pbRanks = new ArrayList<>(ranks.size());
        for (RankItemInt rank : ranks) {
            PlayerLordDto lordDto = lordMap.get(rank.getLordId());
            CommonPb.CrossRankItem.Builder builder = CrossPlayerPbHelper.buildCrossRankItem(lordDto, rank);
            builder.setCamp(rank.getForces());
            pbRanks.add(builder.build());
        }
        return pbRanks;
    }

    public static CrossAwardPb createCrossAwardPb(List<List<Integer>> rewardList, AwardFrom from, boolean isAdd) {
        CrossAwardPb.Builder awardBuilder = CrossAwardPb.newBuilder();
        List<Award> awarPbList = rewardList.stream().filter(l -> l.size() >= 3)
                .map(l -> PbHelper.createAwardPb(l.get(0), l.get(1), l.get(2))).collect(Collectors.toList());
        awardBuilder.addAllAward(awarPbList);
        awardBuilder.setFrom(from.getCode());
        awardBuilder.setIsAdd(isAdd);
        return awardBuilder.build();
    }

    public static GamePb3.SyncChatRs createSyncChatRs(CrossChat chat) {
        CommonPb.Chat pbChat = buildChat(chat);
        GamePb3.SyncChatRs.Builder builder = GamePb3.SyncChatRs.newBuilder();
        builder.setChat(pbChat);
        return builder.build();
    }

    public static CommonPb.Chat buildChat(CrossChat crossChat) {
        CommonPb.Chat.Builder builder = buildChatCommon(crossChat);
        if (crossChat instanceof CrossSystemChat) {
            CrossSystemChat crossSystemChat = (CrossSystemChat) crossChat;
            buildCrossSystemChat(builder, crossSystemChat);
        } else if (crossChat instanceof CrossRoleChat) {
            CrossRoleChat crossRoleChat = (CrossRoleChat) crossChat;
            if (crossChat.getMsgType() == CrossChatConst.ChatMsgType.PLAYER_CHAT) {
                buildCrossRoleChat(builder, crossRoleChat);
            } else if (crossChat.getMsgType() == CrossChatConst.ChatMsgType.PLAYER_SHARE_POS) {
                buildCrossRoleSharePos(builder, crossRoleChat);
            }
        } else {
            LogUtil.error("chatMsgId: ", crossChat.getChatMsgId(), " chatType not found!!!");
        }
        return builder.build();
    }

    private static void buildCrossRoleSharePos(CommonPb.Chat.Builder builder, CrossRoleChat roleChat) {
        buildCrossChatSender(builder, roleChat);
        builder.setChatId(Integer.parseInt(roleChat.getMsg()));
        List<String> extParam = roleChat.getExtParam();
        if (CheckNull.nonEmpty(extParam)) {
            int area = Integer.parseInt(extParam.get(0));
            builder.setArea(area);
            builder.addAllParam(extParam.subList(1, extParam.size()));
        }
    }

    private static void buildCrossRoleChat(CommonPb.Chat.Builder builder, CrossRoleChat roleChat) {
        buildCrossChatSender(builder, roleChat);
        builder.setMsg(roleChat.getMsg());
        if (CheckNull.nonEmpty(roleChat.getExtParam())) {
            builder.addAllMyParam(roleChat.getExtParam());
        }
    }

    private static void buildCrossSystemChat(CommonPb.Chat.Builder builder, CrossSystemChat sysChat) {
        builder.setChatId(sysChat.getChatId());
        if (Objects.nonNull(sysChat.getParam())) {
            for (Object param : sysChat.getParam()) {
                builder.addParam(String.valueOf(param));
            }
        }
    }

    private static void buildCrossChatSender(CommonPb.Chat.Builder builder, CrossRoleChat roleChat) {
        builder.setName(roleChat.getNick());
        builder.setPortrait(roleChat.getPortrait());
        builder.setPortraitFrame(roleChat.getPortraitFrame());
        builder.setLv(roleChat.getLevel());
        builder.setCamp(roleChat.getCamp());
        builder.setJob(roleChat.getCampJob());
        builder.setMainServerId(roleChat.getServerId());
        builder.setLordId(roleChat.getLordId());
        builder.setArea(roleChat.getMapId());
        builder.setRanks(roleChat.getRanks());
        builder.setBubbleId(roleChat.getBubbleId());
    }

    private static CommonPb.Chat.Builder buildChatCommon(CrossChat chat) {
        CommonPb.Chat.Builder builder = CommonPb.Chat.newBuilder();
        builder.setTime((int) (chat.getTime() / 1000));
        builder.setChannel(ChatConst.CHANNEL_CROSS);
        builder.setRoomId(chat.getRoomId());
        builder.setChlId(chat.getChlId());
        builder.setChatMsgId(chat.getChatMsgId());
        builder.setStyle(chat.getStyle());
        return builder;
    }

    public static CommonPb.GameMilitarySituationPb buildGameMilitarySituation(MilitarySituation dto) {
        CommonPb.GameMilitarySituationPb.Builder builder = CommonPb.GameMilitarySituationPb.newBuilder();
        builder.setAtkCamp(dto.getAtkCamp());
        builder.setAtkLord(dto.getAtkLord());
        builder.setAtkName(dto.getAtkName());
        builder.setPos(dto.getAtkPos());
        builder.setAtkCamp(dto.getAtkCamp());
        builder.setAtkTime(dto.getAtkTime());
        builder.setBattleType(dto.getBattleType());
        builder.setStatus(dto.getStatus());
        Optional.ofNullable(dto.getParams()).ifPresent(to -> builder.addAllParams(to));

        return builder.build();
    }


    public static <T> Base.Builder createBase(int cmd, int code, long lordId, GeneratedExtension<Base, T> ext, T msg) {
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(cmd);
        baseBuilder.setCode(code);
        baseBuilder.setLordId(lordId);
        if (msg != null && ext != null) {
            baseBuilder.setExtension(ext, msg);
        }
        return baseBuilder;
    }

    public static <T> Base.Builder createBase(int cmd, long lordId, GeneratedExtension<Base, T> ext, T msg) {
        return createBase(cmd, GameError.OK.getCode(), lordId, ext, msg);
    }
}
