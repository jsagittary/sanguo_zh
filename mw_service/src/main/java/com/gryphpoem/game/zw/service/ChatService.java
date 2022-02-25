package com.gryphpoem.game.zw.service;

import com.gryphpoem.cross.chat.RpcChatService;
import com.gryphpoem.cross.chat.dto.CrossRoleChat;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.battle.BaseMapBattle;
import com.gryphpoem.game.zw.gameplay.local.world.map.CityMapEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.WFCityMapEntity;
import com.gryphpoem.game.zw.crosssimple.util.PbCrossUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFishMgr;
import com.gryphpoem.game.zw.dataMgr.StaticMailDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticVipDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CrossPb.SendCrossChatRq;
import com.gryphpoem.game.zw.pb.GamePb3.*;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.RedPacketRole;
import com.gryphpoem.game.zw.resource.domain.s.StaticMail;
import com.gryphpoem.game.zw.resource.domain.s.StaticVip;
import com.gryphpoem.game.zw.resource.pojo.Mail;
import com.gryphpoem.game.zw.resource.pojo.chat.Chat;
import com.gryphpoem.game.zw.resource.pojo.chat.ChatDialog;
import com.gryphpoem.game.zw.resource.pojo.chat.RoleChat;
import com.gryphpoem.game.zw.resource.pojo.chat.ShareChat;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author TanDonghai
 * @ClassName ChatService.java
 * @Description 聊天相关
 * @date 创建时间：2017年4月6日 下午5:44:51
 */
@Service
public class ChatService {

    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private WarDataManager warDataManager;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;
    @Autowired
    private CrossChatService crossChatService;

    /**
     * 获取最近的聊天记录，默认返回本阵营的聊天记录
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetChatRs getChat(long roleId, GetChatRq req) throws MwException {
        int now = TimeHelper.getCurrentSecond();
        final Player player = playerDataManager.checkPlayerIsExist(roleId);
        GetChatRs.Builder builder = GetChatRs.newBuilder();
        // 区域聊天
        List<CommonPb.Chat> areaChat = chatDataManager.getAreaChat(player.lord.getArea());
        areaChat.stream()
                .filter(e -> player.lord.getCamp() == e.getCamp() && !player.isInBlacklist(e.getLordId()))
                .sorted(Comparator.comparingInt(CommonPb.Chat::getTime))
                .forEach(c -> checkSilence(now, builder, c));
        // 阵营聊天
        List<CommonPb.Chat> campChat = chatDataManager.getCampChat(player.lord.getCamp());
        campChat.stream()
                .filter(e -> !player.isInBlacklist(e.getLordId()))
                .sorted(Comparator.comparingInt(CommonPb.Chat::getTime))
                .forEach(c -> checkSilence(now, builder, c));

        // 世界聊天
        List<CommonPb.Chat> worldChat = chatDataManager.getWorldChat();
        worldChat.stream()
                .filter(e -> !player.isInBlacklist(e.getLordId()))
                .sorted(Comparator.comparingInt(CommonPb.Chat::getTime))
                .forEach(c -> checkSilence(now, builder, c));

        // 大喇叭
        chatDataManager.clearExceedRoleWorldChat();// 清理大喇叭
        List<CommonPb.Chat> worldRoleChat = chatDataManager.getWorldRoleChat();
        worldRoleChat.forEach(c -> builder.addChat(c));
        // 红包
        chatDataManager.getAndClearRedPacket().values().forEach(rp -> {
            // 只给系统红包
            int time = 0;
            RedPacketRole role = rp.getRole().get(roleId);
            if (role != null) {
                time = role.getTime();
            }
            builder.addRedPacket(PbHelper.createRedPacketShowPb(rp, time));
        });

        return builder.build();
    }

    /**
     * 禁言的玩家,发送的历史聊天不显示
     *
     * @param now 现在的时间戳
     * @param builder GetChatRs.Builder
     * @param c CommonPb.Chat
     */
    private void checkSilence(int now, GetChatRs.Builder builder, CommonPb.Chat c) {
        // 禁言的玩家,发送的历史聊天不显示
        Player player = playerDataManager.getPlayer(c.getLordId());
        if (!CheckNull.isNull(player)) {
            if (player.lord.getSilence() > 0) {
                if (now > player.lord.getSilence()) {
                    builder.addChat(c);
                }
            } else {
                builder.addChat(c);
            }
        }
        //系统消息
        else if(c.getChatId()>0){
            builder.addChat(c);
        }
    }

    /**
     * @param roleId
     * @param chatId
     * @return GetFmsGelTunChatsRs
     * @throws MwException
     * @Title: getFmsGelTunChats
     * @Description: 获取名将转盘最新的推送消息(也可兼容其他世界推送的查询)
     */
    public GetFmsGelTunChatsRs getFmsGelTunChats(long roleId, int chatId) throws MwException {
        GetFmsGelTunChatsRs.Builder builder = GetFmsGelTunChatsRs.newBuilder();
        int beginTime = 0;
        int endTime = 0;
        ActivityBase plan = StaticActivityDataMgr.getActivityByType(ActivityConst.FAMOUS_GENERAL_TURNPLATE);
        if (plan != null) {
            Date begin = plan.getBeginTime();
            Date end = plan.getEndTime();
            beginTime = TimeHelper.dateToSecond(begin);
            endTime = TimeHelper.dateToSecond(end);
        }

        // 世界聊天
        List<CommonPb.Chat> worldChat = chatDataManager.getWorldChat();

        for (CommonPb.Chat c : worldChat) {
            if (c.getChatId() == chatId && c.getTime() > beginTime && c.getTime() < endTime) {
                builder.addChats(c);
            }
            if (builder.getChatsList().size() >= Constant.FAMOUS_GENERAL_TURNPLATE_CHATS_CNT) {
                break;
            }
        }

        return builder.build();
    }

    /**
     * 发送聊天
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public SendChatRs sendChat(long roleId, SendChatRq req) throws Exception {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int now = TimeHelper.getCurrentSecond();

        int channel = req.getChannel();
        List<String> msg = req.getContentList();
        int chatId = 0;
        if (req.hasChatId()) {
            chatId = req.getChatId();
        }

        SendChatRs.Builder builder = SendChatRs.newBuilder();

        if (now - player.chatTime < 5 && player.account.getIsGm()==0) {
            throw new MwException(GameError.CHAT_CD.getCode(), "玩家发送聊天过于频繁, roleId:", roleId, ", chatTime:",
                    player.chatTime);
        }

        if (chatId == 0) {// 普通聊天

            // 禁言只对普通聊天生效
            int silenceTime = player.lord.getSilence();
            if (silenceTime > 0) {
                if (silenceTime == 1 ){
                    throw new MwException(GameError.CHAT_SILENCE.getCode(), "玩家已被禁言，不能发送聊天, roleId:", roleId,
                             ", silence:", silenceTime);
                }
                if (silenceTime > now) {
                    builder.setSilenceTime(String.valueOf(silenceTime));
                    return builder.build();
                }
            }

            if (msg.isEmpty()) {
                throw new MwException(GameError.INVALID_PARAM.getCode(), "聊天内容为空, roleId:", roleId);
            }

            String content = msg.get(0);
            if (content.length() > (channel == ChatConst.CHANNEL_WORLD ? ChatConst.WORLD_CHAT_MAX_LENGTH
                    : ChatConst.MAX_CHAT_LEN) && player.account.getIsGm()==0) {
                throw new MwException(GameError.MAX_CHAT_LENTH.getCode(), "聊天内容过长, roleId:", roleId, ", len:",
                        content.length(), ", limit:", ChatConst.MAX_CHAT_LEN);
            }

            content = EmojiHelper.filterEmoji(content);

            if (ChatHelper.isCorrect(content)) {
                content = "*******";
            }
            //如果是敏感词转*号
            if(player.account.getIsGm()==0){
                content = ChatHelper.sensitive(content);
            }
            
            RoleChat chat = (RoleChat) createRoleChat(player, content);
            chat.setChannel(channel);

            // 禁言相关
            boolean silence = false;
            if (channel == ChatConst.CHANNEL_WORLD || channel == ChatConst.CHANNEL_CAMP
                    || channel == ChatConst.CHANNEL_AREA || channel == ChatConst.CHANNEL_CROSS) {
                boolean compare = false;
                player.lastChats.offer(content);
                if (content != null && content.length() > ChatConst.CHECK_CHAT_MIN_CNT) {
                    while (player.lastChats.size() > ChatConst.CHECK_CHAT_COMPARE_CNT.get(0)) {
                        player.lastChats.remove();
                    }
                    compare = true;
                }

                if (compare
                        && ChatHelper.isSamely(player.lastChats,
                                ChatConst.CHECK_CHAT_RATE) >= ChatConst.CHECK_CHAT_COMPARE_CNT.get(1)
                        && player.lord.getVip() < ChatConst.CHECK_SILENCE_VIP) { // VIP等级小于指定等级的玩家才会禁言封号
                    sendScreen(chat, player);
                    // 禁言
                    player.lord.setSilence(
                            TimeHelper.getCurrentSecond() + ChatConst.CHAT_SILENCE_TIME * TimeHelper.MINUTE_S);
                    player.lastChats.clear();
                    silence = true;
                }
            }
            // 服务器id
            int serverID = DataResource.ac.getBean(ServerSetting.class).getServerID();
            boolean isLoud = req.hasStyle() && req.getStyle() == 1; // 是否是大喇叭
            if (channel == ChatConst.CHANNEL_WORLD) {// 世界频道
                if (isLoud) {
                    checkLoudCondition(player, now, chat);
                    chatDataManager.sendWorldChat(chat, true);
                } else if (!silence) {
                    chatDataManager.sendWorldChat(chat, false);
                }
                LogUtil.commonChat(ChatConst.WORLD_LOG, chat.getStyle(), player.account.getServerId(),
                        player.lord.getNick(), player.roleId, content, serverID, 0, null, null);
            } else if (channel == ChatConst.CHANNEL_GM) {// GM
            } else if (channel == ChatConst.CHANNEL_AREA) {// 区域
                if ((!silence)) {
                    chatDataManager.sendCampChat(chat, player.lord.getCamp(), player.lord.getArea());
                    //新埋点
                    LogUtil.commonChat(ChatConst.AREA_LOG, chat.getStyle(), player.account.getServerId(),
                            player.lord.getNick(), player.roleId, content, serverID, player.lord.getArea(), null, null);
                }
            } else if (channel == ChatConst.CHANNEL_CAMP) {// 阵营、国家
                if (!silence) {
                    chatDataManager.sendCampChat(chat, player.lord.getCamp(), 0);
                    LogUtil.commonChat(ChatConst.CAMP_LOG, chat.getStyle(), player.account.getServerId(),
                            player.lord.getNick(), player.roleId, content, serverID, 0, null, null);
                }
            } else if (channel == ChatConst.CHANNEL_CROSS) { // 跨服聊天
                crossChatService.sendCrossRoleChat(player, chat, req);
            } else if (channel == ChatConst.CHANNEL_PRIVATE) {// 私聊
                long target = 0;
                if (!req.hasTarget()) {
                    throw new MwException(GameError.INVALID_PARAM.getCode(), "私聊信息没掉私聊对象, roleId:", roleId);
                }
                if (now - player.pChatTime < Constant.PRIVATE_CHAT_INTERVAL && player.account.getIsGm()==0) {
                    throw new MwException(GameError.PRIVATE_CHAT_CD.getCode(), "玩家发送私聊天过于频繁, roleId:", roleId,
                            ", chatTime:", player.chatTime);
                }
                target = req.getTarget();
                Player targetP = playerDataManager.checkPlayerIsExist(target);
                // 黑名单的判断
                if (player.isInBlacklist(target)) {
                    throw new MwException(GameError.BLACKLIST_IN_TARGET.getCode(), "对方在你黑名单中, roleId:", roleId,
                            ", targetId:", target);
                }
                if (targetP.isInBlacklist(roleId)) {
                    throw new MwException(GameError.BLACKLIST_IN_YOU.getCode(), "你在对方黑名单中, roleId:", roleId,
                            ", targetId:", target);
                }
                // 不同阵营私聊 消耗金币
                if (targetP.lord.getCamp() != player.lord.getCamp()) {
                    rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                            Constant.PRIVATE_CHAT_COST_GOLD, AwardFrom.PRIVATE_CHAT_COST);
                }

                sendPrivateChat(chat, player, targetP);
                player.pChatTime = now;
                // if (!sendPrivateChat(chat, target)) {
                // throw new MwException(GameError.CHAT_TARGET_NOT_ONLINE.getCode(), "私聊玩家不在线, roleId:", roleId,
                // ", target:", target);
                // }

                LogUtil.commonChat(ChatConst.PRIVATE_LOG, -1, player.account.getServerId(), player.lord.getNick(),
                        player.roleId, content, serverID, 0, targetP.lord.getNick(), targetP.roleId);
            }
        } else {// 分享,带模板
            if (!StaticMailDataMgr.hasChat(chatId)) {
                throw new MwException(GameError.CHAT_ID_CONFIG.getCode(), "聊天id未配置, roleId:", roleId, ", chatId:",
                        chatId);
            }
            Chat chat = createShareChat(player, chatId, msg);
            // 特殊处理
            processSpecialChatId(roleId, req, chat, player, now, chatId, builder);
            if(builder.getChatHelpCnt()!=-1) {
                if (channel == ChatConst.CHANNEL_WORLD) {// 世界频道
                    chatDataManager.sendWorldChat(chat);
                } else if (channel == ChatConst.CHANNEL_GM) {// GM
                } else if (channel == ChatConst.CHANNEL_AREA) {// 区域
                    chatDataManager.sendCampChat(chat, player.lord.getCamp(), player.lord.getArea());
                } else if (channel == ChatConst.CHANNEL_CAMP) {// 阵营、国家
                    chatDataManager.sendCampChat(chat, player.lord.getCamp(), 0);
                } else if (channel == ChatConst.CHANNEL_PRIVATE) {// 私聊
                }
            }
        }

        player.chatTime = now;

        return builder.build();
    }

    /**
     * 大喇叭检测道具
     * 
     * @param player
     * @param now
     * @param chat
     * @throws MwException
     */
    private void checkLoudCondition(Player player, int now, RoleChat chat) throws MwException {
        // 检测道具
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, PropConstant.PROP_WORLD_SPEAK, 1,
                AwardFrom.USE_PROP);
        String[] myParam = new String[1];
        myParam[0] = String.valueOf(now + Constant.PLAYER_WORLD_CHAT_SHOW_TIME);
        chat.setMyParam(myParam);
        chat.setStyle(1);
    }

    private void processSpecialChatId(long roleId, SendChatRq req, Chat chat, Player player, int now, int chatId,
            SendChatRs.Builder builder) throws Exception {
        if (ChatConst.SENDCHAT_HELP_CHATID_SET.contains(chatId)) {// 被人锤了叫人帮忙,要锤别人了叫人帮忙
            if (now - player.chatTime < 5) {
                throw new MwException(GameError.CHAT_HELP_CD.getCode(), "请求支援过于频繁, roleId:", roleId, ", chatTime:",
                        player.chatTime);
            }
            if (req.hasBattleId()) {
                int battleId = req.getBattleId();
                Battle battle = findBattleById(battleId);

                if (battle != null) {
                    if (ChatConst.SENDCHAT_HELP_DEF_CHATID_SET.contains(chatId)) {
                        if (battle.getDefHelpChatCnt() >= 3) {
                            throw new MwException(GameError.PARAM_ERROR.getCode(), "防守帮助呼叫已经超过上限, roleId:", roleId,
                                    ", helpCnt:", battle.getDefHelpChatCnt());
                        }
                        // 防守者判断
                        if (chatId == ChatConst.CHAT_CAMP_DEF_SEEK_HELP && battle.getDefLordId() != roleId) {
                            throw new MwException(GameError.PARAM_ERROR.getCode(), "阵营战防守呼叫城主才能进行, roleId:", roleId,
                                    ", helpCnt:", battle.getDefHelpChatCnt());
                        }
                        battle.setDefHelpChatCnt(battle.getDefHelpChatCnt() + 1);
                        builder.setChatHelpCnt(battle.getDefHelpChatCnt());
                    } else if (ChatConst.SENDCHAT_HELP_ATK_CHATID_SET.contains(chatId)) {
                        if (battle.getAtkHelpChatCnt() >= 3) {
                            throw new MwException(GameError.PARAM_ERROR.getCode(), "攻击帮助呼叫已经超过上限, roleId:", roleId,
                                    ", helpCnt:", battle.getAtkHelpChatCnt());
                        }
                        // 进攻者判断
                        if (chatId == ChatConst.CHAT_CAMP_ATK_SEEK_HELP && battle.getAtkLordId() != roleId) {
                            throw new MwException(GameError.PARAM_ERROR.getCode(), "阵营攻击守呼叫只能发起者才能进行, roleId:", roleId,
                                    ", helpCnt:", battle.getAtkHelpChatCnt());
                        }
                        battle.setAtkHelpChatCnt(battle.getAtkHelpChatCnt() + 1);
                        builder.setChatHelpCnt(battle.getAtkHelpChatCnt());
                    } else if (chatId == ChatConst.CHAT_SUPER_MINE_DEF_HELP
                            && battle.getType() == WorldConstant.BATTLE_TYPE_SUPER_MINE) {// 超级矿点的增援
                        Integer cnt = battle.getHelpChatCnt().get(roleId);
                        int chatCnt = cnt == null ? 0 : cnt.intValue(); // 获取自己的喊话次数
                        if (chatCnt >= 3) {
                            throw new MwException(GameError.PARAM_ERROR.getCode(), "攻击帮助呼叫已经超过上限, roleId:", roleId,
                                    ", helpCnt:", battle.getAtkHelpChatCnt());
                        }
                        chatCnt++;
                        battle.getHelpChatCnt().put(roleId, chatCnt);
                        builder.setChatHelpCnt(chatCnt);
                    }
                }
            }
        } else if (ChatConst.CHAT_SHARE_POS == chatId) {
            if (now - player.chatTime < ChatConst.SHARE_CHAT_CD) {
                throw new MwException(GameError.CHAT_CD.getCode(), "分享坐标过于频繁, roleId:", roleId, ", chatTime:",
                        player.chatTime, ", now:", now);
            }
            if (req.getChannel() == ChatConst.CHANNEL_CROSS) {
                crossChatService.shareCrossPos(player, req);
            }
        } else if (chatId == ChatConst.CHAT_HERO_SHARE || chatId == ChatConst.CHAT_MEDAL_SHARE || chatId == ChatConst.CHAT_TREASURE_WARE_SHARE) {
            //校验每日分享次数是否
            StaticVip staticVip = StaticVipDataMgr.getVipMap(player.lord.getVip());
            if (null == staticVip || null == staticVip.getShare() || staticVip.getShare().isEmpty()) {
                throw new MwException(GameError.VIP_CONFIG_NOT_EXIST.getCode(), "根据当前vip等级获取对应配置异常, roleId:", player.roleId, ", chatTime:",
                        player.chatTime, "vip:", player.lord.getVip());
            }

            int times = 0;
            int playerConstant = 0;
            for (List<Integer> tem : staticVip.getShare()) {
                if (chatId == tem.get(0)) {
                    times = tem.get(1);
                    switch (tem.get(0)) {
                        case ChatConst.CHAT_HERO_SHARE:
                            playerConstant = PlayerConstant.DAILY_HERO_SHARE_CNT;
                            break;
                        case ChatConst.CHAT_MEDAL_SHARE:
                            playerConstant = PlayerConstant.DAILY_MEDAL_SHARE_CNT;
                            break;
                        case ChatConst.CHAT_TREASURE_WARE_SHARE:
                            playerConstant = PlayerConstant.DAILY_TREASURE_WARE_SHARE_CNT;
                            break;
                        default:
                            break;
                    }

                    break;
                }
            }

            if (playerConstant != 0)
                this.checkHeroAndBookShare(playerConstant, times, player, builder);

        } else if (chatId == ChatConst.CHAT_WAR_FIRE_ATK_HELP || chatId == ChatConst.CHAT_WAR_FIRE_DEF_HELP) {
            if (req.getChannel() == ChatConst.CHANNEL_CROSS) {
                crossChatService.shareCrossPos(player, req);
            } else {
                CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
                CityMapEntity cityEntity = cMap.getCityMapEntityByCityId(req.getBattleId());
                if (Objects.nonNull(cityEntity) && cityEntity instanceof WFCityMapEntity) {
                    WFCityMapEntity wfEntity = (WFCityMapEntity) cityEntity;
                    int camp = player.lord.getCamp();
                    int sendTime = wfEntity.getStatusMap().getOrDefault(camp, 0);
                    if (sendTime > 0 && sendTime > now) {
                        builder.setChatHelpCnt(-1);
                        builder.setSilenceTime(String.valueOf(sendTime));
                        return;
                    }
                    // 下次请求支援的CD
                    wfEntity.getStatusMap().put(camp, now + WorldConstant.WAR_FIRE_CITY_HELP_CD);
                }
            }
        } else if (chatId == ChatConst.CHAT_FISHING_SHARE_FISH){
            int times = player.getFishingData().getShareTimes();
            if(times >= StaticFishMgr.getShareLogLimit()){
                throw new MwException(GameError.FISHING_SHARE_FISH_LIMIT.getCode(),GameError.err(roleId,"分享鱼次数达到上限",player.getFishingData().getShareTimes()));
            }
            times ++;
            player.getFishingData().setShareTimes(times);
            builder.setChatHelpCnt(StaticFishMgr.getShareLogLimit() - times);
        }
    }

    /**
     * 校验英雄或兵书分享、宝具分享
     * @throws MwException
     */
    private void checkHeroAndBookShare(int payConstant, int times, Player player, SendChatRs.Builder builder) {
        int number = player.getMixtureDataById(payConstant);
        if (number >= times) {
            builder.setChatHelpCnt(-1);
        } else {
            player.addMixtureData(payConstant, 1);
            builder.setChatHelpCnt(times - player.getMixtureDataById(payConstant));
        }
    }

//    private boolean maxCnt(Player player, int chatId, SendChatRs.Builder builder) {
//        for (List<Integer> cntlist:ChatConst.CHAT_SHARE_CNT){
//            if(chatId == cntlist.get(0)&&chatId==ChatConst.CHAT_HERO_SHARE){
//                if (player.getMixtureDataById(PlayerConstant.DAILY_HERO_SHARE_CNT)<cntlist.get(1)) {
//                    player.addMixtureData(PlayerConstant.DAILY_HERO_SHARE_CNT, 1);
//                    builder.setChatHelpCnt(cntlist.get(1)-player.getMixtureDataById(PlayerConstant.DAILY_HERO_SHARE_CNT));
//                    return true;
//                }
//            }
//            if(chatId == cntlist.get(0)&&chatId==ChatConst.CHAT_MEDAL_SHARE){
//                if (player.getMixtureDataById(PlayerConstant.DAILY_MEDAL_SHARE_CNT)<cntlist.get(1)) {
//                    player.addMixtureData(PlayerConstant.DAILY_MEDAL_SHARE_CNT, 1);
//                    builder.setChatHelpCnt(cntlist.get(1)-player.getMixtureDataById(PlayerConstant.DAILY_MEDAL_SHARE_CNT));
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
    
    private Battle findBattleById(int battleId) {
        Battle battle = warDataManager.getBattleMap().get(battleId);
        if (battle != null) {
            return battle;
        }
        battle = warDataManager.getSpecialBattleMap().get(battleId);
        if (battle != null) {
            return battle;
        }
        for (CrossWorldMap cMap : crossWorldMapDataManager.getCrossWorldMapMap().values()) {
            BaseMapBattle baseMapBattle = cMap.getMapWarData().getAllBattles().get(battleId);
            if (baseMapBattle != null) {
                return baseMapBattle.getBattle();
            }
        }
        return null;
    }

    /**
     * 分享战报
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public ShareReportRs shareReport(long roleId, ShareReportRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int now = TimeHelper.getCurrentSecond();
        if (now - player.chatTime < 5) {
            throw new MwException(GameError.CHAT_CD.getCode(), "玩家发送聊天过于频繁, roleId:", roleId, ", chatTime:",
                    player.chatTime);
        }

        int keyId = 0;
        int channel = req.getChannel();

        Chat chat = null;
        if (req.hasKeyId()) {
            keyId = req.getKeyId();
            Mail mail = player.mails.get(keyId);
            if (mail == null) {
                throw new MwException(GameError.SHARE_KEY_NOT_FOUND.getCode(), "分享战报，id不存在, roleId:", roleId,
                        ", keyId:", keyId);
            }
            Map<Integer, StaticMail> mailMap = StaticMailDataMgr.getMailMap();
            StaticMail staticMail = mailMap.get(mail.getMoldId());
            if (staticMail == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "分享战报，mailId不存在, roleId:", roleId, ", mailId:",
                        mail.getMoldId());
            }
            int chatId = staticMail.getChatId();
            if (chatId == 0) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "分享战报，邮件不能分享, roleId:", roleId, ", mailId:",
                        mail.getMoldId());
            }
            List<String> params = req.getParamsList();

            // if (chatId == ChatConst.CHAT_ROLE_SCOUT) { // 侦查
            // CommonPb.MailScout scout = mail.getScout();
            // if (scout == null) {
            // throw new MwException(GameError.SHARE_SCOUT_NOT_EXIST.getCode(), "分享侦查，侦查不存在, roleId:", roleId,
            // ", id:", keyId);
            // }
            // params.add(player.lord.getNick());
            // params.add(mail.gettParam().get(2));
            // } else if (chatId == ChatConst.CHAT_SHARE_REPORT) { // 战报
            // CommonPb.Report report = mail.getReport();
            // if (report == null) {
            // throw new MwException(GameError.SHARE_REPORT_NOT_EXIST.getCode(), "分享战报，战报不存在, roleId:", roleId,
            // ", id:", keyId);
            // }
            // CommonPb.RptAtkPlayer rptPlayer = report.getRptPlayer();
            // CommonPb.RptMan attack = rptPlayer.getAttack();
            // CommonPb.RptMan defMan = rptPlayer.getDefMan();
            // params.add(attack.getName());
            // params.add(defMan.getName());
            // } else if (chatId == ChatConst.CHAT_CITY_REPORT) { // 城战
            // CommonPb.Report report = mail.getReport();
            // if (report == null) {
            // throw new MwException(GameError.SHARE_CITY_NOT_EXIST.getCode(), "分享城战，战报不存在, roleId:", roleId,
            // ", id:", keyId);
            // }
            // CommonPb.RptAtkPlayer rptPlayer = report.getRptPlayer();
            // CommonPb.RptMan attack = rptPlayer.getAttack();
            // CommonPb.RptCity defCity = rptPlayer.getDefCity();
            // params.add(attack.getName());
            // params.add(String.valueOf(defCity.getCityId()));
            // } else if (chatId == ChatConst.CHAT_BANDIT_REPORT) { // 匪战
            // CommonPb.Report report = mail.getReport();
            // if (report == null) {
            // throw new MwException(GameError.SHARE_BANDIT_NOT_EXIST.getCode(), "分享匪战，战报不存在, roleId:", roleId,
            // ", id:", keyId);
            // }
            // CommonPb.RptAtkBandit rptAtkBandit = report.getRptBandit();
            // CommonPb.RptBandit defend = rptAtkBandit.getDefend();
            // params.add(String.valueOf(defend.getBanditId()));
            // }
            chatDataManager.addShareReport(player, mail);
            chat = createReportShare(player, mail.getMoldId(), keyId, params, chatId);
        }

        if (channel == ChatConst.CHANNEL_WORLD) {// 世界
            chatDataManager.sendWorldChat(chat);
        } else {// 阵营、国家
            chatDataManager.sendCampChat(chat, player.lord.getCamp(), 0);
        }

        player.chatTime = now;

        ShareReportRs.Builder builder = ShareReportRs.newBuilder();
        return builder.build();
    }

    /**
     * 获取私聊消息
     *
     * @param roleId
     * @param targetId
     * @return
     * @throws MwException
     */
    public GetPrivateChatRs getPrivateChat(long roleId, long targetId) throws MwException {
        playerDataManager.checkPlayerIsExist(roleId);
        playerDataManager.checkPlayerIsExist(targetId);
        List<CommonPb.Chat> chatList = chatDataManager.getPrivateChat(roleId, targetId);
        GetPrivateChatRs.Builder builder = GetPrivateChatRs.newBuilder();
        if (!CheckNull.isEmpty(chatList)) {
            builder.addAllChat(chatList);
        }
        return builder.build();
    }

    /**
     * 获取会话
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetDialogRs getDialog(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Map<Long, ChatDialog> dialog = chatDataManager.getDialog(roleId);
        GetDialogRs.Builder builder = GetDialogRs.newBuilder();
        if (!CheckNull.isEmpty(dialog)) {
            for (ChatDialog d : dialog.values()) {
                Player tp = playerDataManager.getPlayer(d.getTargetId());
                if (tp == null) continue;
                d.setChat(d.getChat().toBuilder().setPortraitFrame(tp.getDressUp().getCurPortraitFrame()).build());

                boolean falg = true;
                // 判断是否是 阵营邮件会话
                /*if(d.getIsCampChatDia() == ChatConst.IS_CAMP_MAIL_CHAT_DIALOG) {
                    falg = false;
                	List<CommonPb.Chat> chatList = chatDataManager.getPrivateChat(roleId, d.getTargetId());
                	for(CommonPb.Chat c : chatList) {
                		if(c.getIsCampChat() != ChatConst.IS_CAMP_MAIL_CHAT) {//有私聊
                			falg = true;
                			break;
                		}
                	}
                }*/
                if (falg) {
                    builder.addDialogs(PbHelper.createChatDialogPb(player, tp, d));
                }
            }
        }
        return builder.build();
    }

    /**
     * 删除会话
     *
     * @param roleId
     * @param targetIds
     * @return
     * @throws MwException
     */
    public DelDialogRs delDialog(long roleId, List<Long> targetIds) throws MwException {
        playerDataManager.checkPlayerIsExist(roleId);
        if (!CheckNull.isEmpty(targetIds)) {
            for (Long targetId : targetIds) {
                playerDataManager.checkPlayerIsExist(targetId);
                chatDataManager.delDialog(roleId, targetId);
            }
        }
        DelDialogRs.Builder builder = DelDialogRs.newBuilder();
        return builder.build();
    }

    /**
     * 已读会话
     *
     * @param roleId
     * @param targetId
     * @return
     * @throws MwException
     */
    public ReadDialogRs readDialog(long roleId, long targetId) throws MwException {
        playerDataManager.checkPlayerIsExist(roleId);
        playerDataManager.checkPlayerIsExist(targetId);
        chatDataManager.readDialog(roleId, targetId);
        ReadDialogRs.Builder builder = ReadDialogRs.newBuilder();
        return builder.build();
    }

    /**
     * 创建战报分享聊天对象
     *
     * @param player
     * @param moldId
     * @param reportKey
     * @param tParam
     * @return
     */
    private Chat createReportShare(Player player, int moldId, int reportKey, List<String> tParam, int chatId) {
        ShareChat chat = new ShareChat();
        chat.setPlayer(player);
        chat.setTime(TimeHelper.getCurrentSecond());
        chat.setMoldId(moldId);
        chat.setParam(tParam);
        chat.setReport(reportKey);
        chat.setChatId(chatId);
        return chat;
    }

    /**
     * 创建角色聊天对象
     *
     * @param player
     * @param msg
     * @return
     */
    public Chat createRoleChat(Player player, String msg) {
        RoleChat chat = new RoleChat();
        chat.setPlayer(player);
        chat.setTime(TimeHelper.getCurrentSecond());
        chat.setMsg(msg);
        return chat;
    }

    /**
     * 发送屏蔽某玩家的消息
     *
     * @param chat
     * @param player
     */
    public void sendScreen(Chat chat, Player player) {
        CommonPb.Chat b = chatDataManager.addWorldChat(chat);
        SyncChatRs.Builder chatBuilder = SyncChatRs.newBuilder();
        chatBuilder.setScreenRoleId(player.roleId);
        chatBuilder.setChat(b);
        Base.Builder builder = PbHelper.createSynBase(SyncChatRs.EXT_FIELD_NUMBER, SyncChatRs.ext, chatBuilder.build());
        Player temp;
        Iterator<Player> it = playerDataManager.getAllOnlinePlayer().values().iterator();
        while (it.hasNext()) {
            temp = it.next();
            if (temp.ctx != null) {
                MsgDataManager.getIns().add(new Msg(temp.ctx, builder.build(), temp.roleId));
            }
        }
    }

    /**
     * 发送私聊消息
     *
     * @param chat
     * @param my
     * @param targetP
     * @return 返回是否发送成功
     */
    private boolean sendPrivateChat(Chat chat, Player my, Player targetP) {
        CommonPb.Chat b = chatDataManager.createPrivateChat(chat, my.roleId, targetP.roleId);
        if (targetP != null && targetP.isLogin) {// 在线消息
            SyncChatRs.Builder chatBuilder = SyncChatRs.newBuilder();
            chatBuilder.setChat(b);
            Base.Builder builder = PbHelper.createSynBase(SyncChatRs.EXT_FIELD_NUMBER, SyncChatRs.ext,
                    chatBuilder.build());
            MsgDataManager.getIns().add(new Msg(targetP.ctx, builder.build(), targetP.roleId));
            return true;
        } else {
            return false;
        }
    }

    /**
     * 发送系统聊天到世界频道
     *
     * @param chatId
     * @param param
     */
    public void sendSysChatOnWorld(int chatId,int camp, Object... param) {
        Chat chat = chatDataManager.createSysChat(chatId,param);
        chat.setCamp(camp);
        if(camp >0){
            chatDataManager.sendCampChat(chat,camp,0);
        }else {
            chatDataManager.sendWorldChat(chat);
        }
    }

    /**
     * 创建玩家根据聊天模版id发送的分享聊天对象
     *
     * @param player
     * @param chatId
     * @param param
     * @return
     */
    private Chat createShareChat(Player player, int chatId, List<String> param) {
        ShareChat chat = new ShareChat();
        chat.setPlayer(player);
        chat.setTime(TimeHelper.getCurrentSecond());
        chat.setChatId(chatId);
        chat.setParam(param);
        return chat;
    }

    /**
     * 发送消息到跨服
     * 
     * @param chat
     * @param player
     * @param isLoud 是否是大喇叭 true表示大喇叭
     * @throws MwException
     */
    public void sendCrossChat(RoleChat chat, Player player, boolean isLoud) throws MwException {
        if (isLoud) {
            int now = TimeHelper.getCurrentSecond();
            checkLoudCondition(player, now, chat);
        }
        CommonPb.Chat chatPb = chat.serBuilder().setMainServerId(serverSetting.getServerID())
                .setMyServerId(player.account.getServerId()).build();
        SendCrossChatRq.Builder builder = SendCrossChatRq.newBuilder();
        builder.setChat(chatPb);
        Base base = PbCrossUtil.createBase(SendCrossChatRq.EXT_FIELD_NUMBER, player.lord.getLordId(),
                SendCrossChatRq.ext, builder.build()).build();
        DataResource.sendMsgToCross(base); // 向跨服发送消息
    }

    /**
     * 获取活动消息
     * @param roleId
     * @return
     * @throws MwException 
     */
    public GetActivityChatRs getActivityChat(long roleId, int activityId) throws MwException {
        playerDataManager.checkPlayerIsExist(roleId);
        GetActivityChatRs.Builder builder = GetActivityChatRs.newBuilder();
        LinkedList<CommonPb.Chat> activityChat = chatDataManager.getActivityChat(activityId);
        int beginTime = 0;
        int endTime = 0;
        ActivityBase plan = StaticActivityDataMgr.getActivityByType(activityId);
        Date begin = plan.getBeginTime();
        Date end = plan.getEndTime();
        beginTime = TimeHelper.dateToSecond(begin);
        endTime = TimeHelper.dateToSecond(end);
        if (!CheckNull.isEmpty(activityChat)) {
            Iterator<CommonPb.Chat> iterator = activityChat.iterator();
            while (iterator.hasNext()){
                CommonPb.Chat c = iterator.next();
                if (c.getTime() <= beginTime || c.getTime() >= endTime) {
                    iterator.remove();//删除过期的消息
                }
            }
            builder.addAllChat(activityChat);
        }
        
        return builder.build();
    }
}
