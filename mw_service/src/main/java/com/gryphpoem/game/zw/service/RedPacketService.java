package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.service.activity.ActivityRedPacketService;
import com.hundredcent.game.ai.util.CheckNull;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticChatDataMgr;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.pb.GamePb3.*;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.RedPacket;
import com.gryphpoem.game.zw.resource.domain.p.RedPacketRole;
import com.gryphpoem.game.zw.resource.domain.s.StaticRedPacket;
import com.gryphpoem.game.zw.resource.domain.s.StaticRedPacketMessage;
import com.gryphpoem.game.zw.resource.domain.s.StaticRedpacketList;
import com.gryphpoem.game.zw.resource.pojo.chat.RoleChat;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.RandomHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName RedPacketService.java
 * @Description 红包处理
 * @date 2018年6月8日
 */
@Service
public class RedPacketService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private ChatService chatService;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private ActivityRedPacketService activityRedPacketService;


    /**
     * 发送系统红包
     *
     * @param redPacketId 红包表id
     * @param redTpe      红包类型 默认0是系统红包，27是红包活动类型
     * @param param
     */
    public void sendSysRedPacket(int redPacketId, int redTpe, String... param) {
        StaticRedPacket sRp = StaticChatDataMgr.getRedPacketById(redPacketId);
        if (sRp == null) return;
        List<List<Integer>> awardIdAndCnt = sRp.getAward();
        if (CheckNull.isEmpty(awardIdAndCnt)) return;
        List<Integer> rewarPond = new ArrayList<>();
        for (List<Integer> idAndCnt : awardIdAndCnt) {
            if (idAndCnt.size() < 2) continue;
            Integer cnt = idAndCnt.get(1);
            if (cnt < 1) continue;
            Integer id = idAndCnt.get(0);
            for (int i = 0; i < cnt; i++) {
                rewarPond.add(id);
            }
        }
        Collections.shuffle(rewarPond);// 打乱顺序
        List<String> paramList = new ArrayList<>();
        if (param != null) {
            for (String p : param) {
                paramList.add(p);
            }
        }
        RedPacket rp = new RedPacket(rewarPond, paramList, sRp.getTime(), sRp.getChatId(), redPacketId, redTpe);
        Map<Integer, RedPacket> redPacketMap = chatDataManager.getAndClearRedPacket();
        redPacketMap.put(rp.getId(), rp);

        // 发送红包消息
        syncRedPacket(rp);

    }

    /**
     * 推送红包消息
     *
     * @param rp
     */
    private void syncRedPacket(RedPacket rp) {
        playerDataManager.getAllOnlinePlayer().values().forEach(p -> {
            if (p.ctx != null) {
                SyncRedPacketRs.Builder rpb = SyncRedPacketRs.newBuilder();
                rpb.setRedPacketShow(PbHelper.createRedPacketShowPb(rp, 0));
                Base.Builder builder = PbHelper.createSynBase(SyncRedPacketRs.EXT_FIELD_NUMBER, SyncRedPacketRs.ext,
                        rpb.build());
                MsgDataManager.getIns().add(new Msg(p.ctx, builder.build(), p.roleId));
            }
        });
    }

    private void syncRedPacketAccept(Player p, RedPacketRole rpr, int rpId) {
        if (p != null && p.isLogin && p.ctx != null) {
            SyncRedPacketAcceptRs.Builder b = SyncRedPacketAcceptRs.newBuilder();
            b.setRole(PbHelper.createRedPacketRolePb(rpr, playerDataManager));
            b.setId(rpId);
            Base.Builder builder = PbHelper.createSynBase(SyncRedPacketAcceptRs.EXT_FIELD_NUMBER,
                    SyncRedPacketAcceptRs.ext, b.build());
            MsgDataManager.getIns().add(new Msg(p.ctx, builder.build(), p.roleId));
        }
    }

    /**
     * 领取红包
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public AcceptRedPacketRs acceptRedPacket(long roleId, AcceptRedPacketRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int rpId = req.getId();// 红包id
        RedPacket redPacket = chatDataManager.getAndClearRedPacket().get(rpId);
        if (redPacket == null) {
            throw new MwException(GameError.RED_PACKET_IS_EXCEED.getCode(), "红包已过期或不存在  roleId:", roleId);
        }
        // 领取红包
        Map<Long, RedPacketRole> roles = redPacket.getRole();
        List<Integer> rewarPond = redPacket.getRewarPond();
        AcceptRedPacketRs.Builder builder = AcceptRedPacketRs.newBuilder();

        if (roles.containsKey(roleId) || rewarPond.size() <= roles.size()) {// 已经领取过了

        } else {// 领取红包
            int msgId = 1;
            StaticRedPacket sRp = StaticChatDataMgr.getRedPacketById(redPacket.getRedPackId());
            if (sRp != null && !CheckNull.isEmpty(sRp.getMessage())) {
                int index = RandomHelper.randomInSize(sRp.getMessage().size());
                msgId = sRp.getMessage().get(index);
            }
            List<Long> roleList = roles.keySet().stream().collect(Collectors.toList());
            int awardId = rewarPond.get(roles.size());
            RedPacketRole rpr = new RedPacketRole(roleId, player.lord.getNick(), awardId, msgId);
            rpr.setPortrait(player.lord.getPortrait());
            roles.put(roleId, rpr);// 添加已领取人员中

            StaticRedpacketList sRpl = StaticChatDataMgr.getRedpacketListById(awardId);
            Award award = null;
            if (sRpl != null) {
                // 给奖励
                award = rewardDataManager.addAwardSignle(player, sRpl.getContent(), AwardFrom.RED_PACKET_AWARD);
            } else {
                // 没有配给1个金币
                LogUtil.error("roleId:", roleId, ", 为配置红包将领:", awardId);
                award = rewardDataManager.addAwardSignle(player, AwardType.MONEY, AwardType.Money.GOLD, 1,
                        AwardFrom.RED_PACKET_AWARD);
            }
            // 推送已领取红包人员推送
            // roleList.stream().forEach(rId -> {
            // Player p = playerDataManager.getPlayer(rId);
            // syncRedPacketAccept(p, rpr, rpId);
            // });

            if( redPacket.getRedType() == ActivityConst.ACT_RED_PACKET){
                StaticRedpacketList listMaxValue = StaticChatDataMgr.getRedpacketListMaxValue(rewarPond);
                if(listMaxValue.getValue() == sRpl.getValue()){
                    StaticRedPacketMessage redPacketMessage = StaticChatDataMgr.getRedPacketMessageMap().get(msgId);
                    String sendName = redPacket.getParam().get(0);
                    if(sendName != null && !player.lord.getNick().equals(sendName)){
                        String format = String.format(redPacketMessage.getDes(), sendName);
                        RoleChat chat = (RoleChat) chatService.createRoleChat(player,format);
                        chat.setChannel(ChatConst.CHANNEL_WORLD);
                        chat.setRedType(redPacket.getRedType());
                        chatDataManager.sendWorldChat(chat, false);
                    }
                }


            }



            builder.addAward(award);
        }
        builder.setRedPacket(PbHelper.createRedPacketPb(redPacket, playerDataManager));
        return builder.build();
    }

    /**
     * 红包详情
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetRedPacketRs getRedPacket(long roleId, GetRedPacketRq req) throws MwException {
        playerDataManager.checkPlayerIsExist(roleId);
        int rpId = req.getId();// 红包id
        RedPacket redPacket = chatDataManager.getAndClearRedPacket().get(rpId);
        if (redPacket == null) {
            throw new MwException(GameError.RED_PACKET_IS_EXCEED.getCode(), "红包已过期或不存在  roleId:", roleId);
        }
        GetRedPacketRs.Builder builder = GetRedPacketRs.newBuilder();
        builder.setRedPacket(PbHelper.createRedPacketPb(redPacket, playerDataManager));
        return builder.build();
    }


    /**
     * 获取红包详情
     *
     * @param roleId
     * @param rq
     * @return
     */
    public GamePb3.GetRedPacketListRs getRedPacketList(long roleId, GamePb3.GetRedPacketListRq rq) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int redType = rq.getRedType();

        GamePb3.GetRedPacketListRs.Builder builder = GamePb3.GetRedPacketListRs.newBuilder();
        // 红包
        chatDataManager.getAndClearRedPacket().values().forEach(rp -> {
            //只给系统红包
            if (rp.getRedType() == redType) {
                builder.addRedPacket(PbHelper.createRedPacketPb(rp, playerDataManager));
            }
        });

        if (redType == ActivityConst.ACT_RED_PACKET) {
            builder.setRecharge(activityRedPacketService.getRedPacketActivityRecharge(player));
        }

        return builder.build();

    }
}
