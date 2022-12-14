package com.gryphpoem.game.zw.service;

import com.gryphpoem.cross.chat.RpcChatService;
import com.gryphpoem.cross.chat.dto.CrossRoleChat;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.battle.BaseMapBattle;
import com.gryphpoem.game.zw.gameplay.local.world.map.CityMapEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.WFCityMapEntity;
import com.gryphpoem.game.zw.crosssimple.util.PbCrossUtil;
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
import com.gryphpoem.game.zw.resource.domain.s.StaticSystem;
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
 * @Description ????????????
 * @date ???????????????2017???4???6??? ??????5:44:51
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

    public void deleteRoleChat(Player player){
        //????????????????????????
        chatDataManager.deleteRoleChat(player.roleId);
        //????????????????????????
        crossChatService.deleteChatByRoleId(player);
    }

    /**
     * ??????????????????????????????????????????????????????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetChatRs getChat(long roleId, GetChatRq req) throws MwException {
        int now = TimeHelper.getCurrentSecond();
        final Player player = playerDataManager.checkPlayerIsExist(roleId);
        GetChatRs.Builder builder = GetChatRs.newBuilder();
        // ????????????
        List<CommonPb.Chat> areaChat = chatDataManager.getAreaChat(player.lord.getArea());
        areaChat.stream()
                .filter(e -> player.lord.getCamp() == e.getCamp() && !player.isInBlacklist(e.getLordId()))
                .sorted(Comparator.comparingInt(CommonPb.Chat::getTime))
                .forEach(c -> checkSilence(now, builder, c));
        // ????????????
        List<CommonPb.Chat> campChat = chatDataManager.getCampChat(player.lord.getCamp());
        campChat.stream()
                .filter(e -> !player.isInBlacklist(e.getLordId()))
                .sorted(Comparator.comparingInt(CommonPb.Chat::getTime))
                .forEach(c -> checkSilence(now, builder, c));

        // ????????????
        List<CommonPb.Chat> worldChat = chatDataManager.getWorldChat();
        worldChat.stream()
                .filter(e -> !player.isInBlacklist(e.getLordId()))
                .sorted(Comparator.comparingInt(CommonPb.Chat::getTime))
                .forEach(c -> checkSilence(now, builder, c));

        // ?????????
        chatDataManager.clearExceedRoleWorldChat();// ???????????????
        List<CommonPb.Chat> worldRoleChat = chatDataManager.getWorldRoleChat();
        worldRoleChat.forEach(c -> builder.addChat(c));
        // ??????
        chatDataManager.getAndClearRedPacket().values().forEach(rp -> {
            // ??????????????????
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
     * ???????????????,??????????????????????????????
     *
     * @param now ??????????????????
     * @param builder GetChatRs.Builder
     * @param c CommonPb.Chat
     */
    private void checkSilence(int now, GetChatRs.Builder builder, CommonPb.Chat c) {
        // ???????????????,??????????????????????????????
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
        //????????????
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
     * @Description: ???????????????????????????????????????(???????????????????????????????????????)
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

        // ????????????
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
     * ????????????
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
            throw new MwException(GameError.CHAT_CD.getCode(), "??????????????????????????????, roleId:", roleId, ", chatTime:",
                    player.chatTime);
        }

        if (chatId == 0) {// ????????????

            // ??????????????????????????????
            int silenceTime = player.lord.getSilence();
            if (silenceTime > 0) {
                if (silenceTime == 1 ){
                    throw new MwException(GameError.CHAT_SILENCE.getCode(), "???????????????????????????????????????, roleId:", roleId,
                             ", silence:", silenceTime);
                }
                if (silenceTime > now) {
                    builder.setSilenceTime(String.valueOf(silenceTime));
                    return builder.build();
                }
            }

            if (msg.isEmpty()) {
                throw new MwException(GameError.INVALID_PARAM.getCode(), "??????????????????, roleId:", roleId);
            }

            String content = msg.get(0);
            if (content.length() > (channel == ChatConst.CHANNEL_WORLD ? ChatConst.WORLD_CHAT_MAX_LENGTH
                    : ChatConst.MAX_CHAT_LEN) && player.account.getIsGm()==0) {
                throw new MwException(GameError.MAX_CHAT_LENTH.getCode(), "??????????????????, roleId:", roleId, ", len:",
                        content.length(), ", limit:", ChatConst.MAX_CHAT_LEN);
            }

            content = EmojiHelper.filterEmoji(content);

            if (ChatHelper.isCorrect(content)) {
                content = "*******";
            }
            //?????????????????????*???
            if(player.account.getIsGm()==0){
                content = ChatHelper.sensitive(content);
            }
            
            RoleChat chat = (RoleChat) createRoleChat(player, content);
            chat.setChannel(channel);

            // ????????????
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
                        && player.lord.getVip() < ChatConst.CHECK_SILENCE_VIP) { // VIP???????????????????????????????????????????????????
                    sendScreen(chat, player);
                    // ??????
                    player.lord.setSilence(
                            TimeHelper.getCurrentSecond() + ChatConst.CHAT_SILENCE_TIME * TimeHelper.MINUTE_S);
                    player.lastChats.clear();
                    silence = true;
                }
            }

            //????????????
            List<List<Integer>> lvConditionList = SystemTabLoader.getListListIntSystemValue(641, "");
            if (CheckNull.nonEmpty(lvConditionList)) {
                if (player.lord.getVip() < 0) {
                    throw new MwException(GameError.FUNCTION_LOCK.getCode(), String.format("??????????????????, roleId:%d, channel:%d, vip:%d", player.roleId, channel, player.lord.getVip()));
                }

                List<Integer> lvCondition = lvConditionList.stream().filter(condition -> CheckNull.nonEmpty(condition) &&
                        condition.get(0) == player.lord.getVip()).findFirst().orElse(null);
                if (lvCondition == null) {
                    //????????????????????????vip??????
                    for (List<Integer> tmp : lvConditionList) {
                        if (CheckNull.isEmpty(tmp)) {
                            continue;
                        }
                        if (CheckNull.isEmpty(lvCondition))
                            lvCondition = tmp;
                        else if (lvCondition.get(0) < tmp.get(0)) {
                            lvCondition = tmp;
                        }
                    }
                }

                int needLv = 0;
                if (CheckNull.nonEmpty(lvCondition) && lvCondition.size() >= 3) {
                    needLv = channel == ChatConst.CHANNEL_PRIVATE ? lvCondition.get(2) : lvCondition.get(1);
                } else {
                    LogUtil.error(String.format("chat level config is empty!!! playerVip:%d", player.lord.getVip()));
                }
                if (needLv > player.lord.getLevel()) {
                    throw new MwException(GameError.FUNCTION_LOCK.getCode(), String.format("??????????????????, roleId:%d, channel:%d, level:%d", player.roleId, channel, player.lord.getLevel()));
                }
            } else {
                LogUtil.error(String.format("chat level config is empty!!!"));
            }

            // ?????????id
            int serverID = DataResource.ac.getBean(ServerSetting.class).getServerID();
            boolean isLoud = req.hasStyle() && req.getStyle() == 1; // ??????????????????
            if (channel == ChatConst.CHANNEL_WORLD) {// ????????????
                if (isLoud) {
                    checkLoudCondition(player, now, chat);
                    chatDataManager.sendWorldChat(chat, true);
                } else if (!silence) {
                    chatDataManager.sendWorldChat(chat, false);
                }
                LogUtil.commonChat(ChatConst.WORLD_LOG, chat.getStyle(), player.account.getServerId(),
                        player.lord.getNick(), player.roleId, content, serverID, 0, null, null);
            } else if (channel == ChatConst.CHANNEL_GM) {// GM
            } else if (channel == ChatConst.CHANNEL_AREA) {// ??????
                if ((!silence)) {
                    chatDataManager.sendCampChat(chat, player.lord.getCamp(), player.lord.getArea());
                    //?????????
                    LogUtil.commonChat(ChatConst.AREA_LOG, chat.getStyle(), player.account.getServerId(),
                            player.lord.getNick(), player.roleId, content, serverID, player.lord.getArea(), null, null);
                }
            } else if (channel == ChatConst.CHANNEL_CAMP) {// ???????????????
                if (!silence) {
                    chatDataManager.sendCampChat(chat, player.lord.getCamp(), 0);
                    LogUtil.commonChat(ChatConst.CAMP_LOG, chat.getStyle(), player.account.getServerId(),
                            player.lord.getNick(), player.roleId, content, serverID, 0, null, null);
                }
            } else if (channel == ChatConst.CHANNEL_CROSS) { // ????????????
                crossChatService.sendCrossRoleChat(player, chat, req);
            } else if (channel == ChatConst.CHANNEL_PRIVATE) {// ??????
                long target = 0;
                if (!req.hasTarget()) {
                    throw new MwException(GameError.INVALID_PARAM.getCode(), "??????????????????????????????, roleId:", roleId);
                }
                if (now - player.pChatTime < Constant.PRIVATE_CHAT_INTERVAL && player.account.getIsGm()==0) {
                    throw new MwException(GameError.PRIVATE_CHAT_CD.getCode(), "?????????????????????????????????, roleId:", roleId,
                            ", chatTime:", player.chatTime);
                }
                target = req.getTarget();
                Player targetP = playerDataManager.checkPlayerIsExist(target);
                // ??????????????????
                if (player.isInBlacklist(target)) {
                    throw new MwException(GameError.BLACKLIST_IN_TARGET.getCode(), "????????????????????????, roleId:", roleId,
                            ", targetId:", target);
                }
                if (targetP.isInBlacklist(roleId)) {
                    throw new MwException(GameError.BLACKLIST_IN_YOU.getCode(), "????????????????????????, roleId:", roleId,
                            ", targetId:", target);
                }
                // ?????????????????? ????????????
                if (targetP.lord.getCamp() != player.lord.getCamp()) {
                    rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                            Constant.PRIVATE_CHAT_COST_GOLD, AwardFrom.PRIVATE_CHAT_COST);
                }

                sendPrivateChat(chat, player, targetP);
                player.pChatTime = now;
                // if (!sendPrivateChat(chat, target)) {
                // throw new MwException(GameError.CHAT_TARGET_NOT_ONLINE.getCode(), "?????????????????????, roleId:", roleId,
                // ", target:", target);
                // }

                LogUtil.commonChat(ChatConst.PRIVATE_LOG, -1, player.account.getServerId(), player.lord.getNick(),
                        player.roleId, content, serverID, 0, targetP.lord.getNick(), targetP.roleId);
            }
        } else {// ??????,?????????
            if (!StaticMailDataMgr.hasChat(chatId)) {
                throw new MwException(GameError.CHAT_ID_CONFIG.getCode(), "??????id?????????, roleId:", roleId, ", chatId:",
                        chatId);
            }
            Chat chat = createShareChat(player, chatId, msg);
            // ????????????
            processSpecialChatId(roleId, req, chat, player, now, chatId, builder);
            if(builder.getChatHelpCnt()!=-1) {
                if (channel == ChatConst.CHANNEL_WORLD) {// ????????????
                    chatDataManager.sendWorldChat(chat);
                } else if (channel == ChatConst.CHANNEL_GM) {// GM
                } else if (channel == ChatConst.CHANNEL_AREA) {// ??????
                    chatDataManager.sendCampChat(chat, player.lord.getCamp(), player.lord.getArea());
                } else if (channel == ChatConst.CHANNEL_CAMP) {// ???????????????
                    chatDataManager.sendCampChat(chat, player.lord.getCamp(), 0);
                } else if (channel == ChatConst.CHANNEL_PRIVATE) {// ??????
                }
            }
        }

        player.chatTime = now;

        return builder.build();
    }

    /**
     * ?????????????????????
     * 
     * @param player
     * @param now
     * @param chat
     * @throws MwException
     */
    private void checkLoudCondition(Player player, int now, RoleChat chat) throws MwException {
        // ????????????
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, PropConstant.PROP_WORLD_SPEAK, 1,
                AwardFrom.USE_PROP);
        String[] myParam = new String[1];
        myParam[0] = String.valueOf(now + Constant.PLAYER_WORLD_CHAT_SHOW_TIME);
        chat.setMyParam(myParam);
        chat.setStyle(1);
    }

    private void processSpecialChatId(long roleId, SendChatRq req, Chat chat, Player player, int now, int chatId,
            SendChatRs.Builder builder) throws Exception {
        if (ChatConst.SENDCHAT_HELP_CHATID_SET.contains(chatId)) {// ????????????????????????,???????????????????????????
            if (now - player.chatTime < 5) {
                throw new MwException(GameError.CHAT_HELP_CD.getCode(), "????????????????????????, roleId:", roleId, ", chatTime:",
                        player.chatTime);
            }
            if (req.hasBattleId()) {
                int battleId = req.getBattleId();
                Battle battle = findBattleById(battleId);

                if (battle != null) {
                    if (ChatConst.SENDCHAT_HELP_DEF_CHATID_SET.contains(chatId)) {
                        if (battle.getDefHelpChatCnt() >= 3) {
                            throw new MwException(GameError.PARAM_ERROR.getCode(), "????????????????????????????????????, roleId:", roleId,
                                    ", helpCnt:", battle.getDefHelpChatCnt());
                        }
                        // ???????????????
                        if (chatId == ChatConst.CHAT_CAMP_DEF_SEEK_HELP && battle.getDefLordId() != roleId) {
                            throw new MwException(GameError.PARAM_ERROR.getCode(), "???????????????????????????????????????, roleId:", roleId,
                                    ", helpCnt:", battle.getDefHelpChatCnt());
                        }
                        battle.setDefHelpChatCnt(battle.getDefHelpChatCnt() + 1);
                        builder.setChatHelpCnt(battle.getDefHelpChatCnt());
                    } else if (ChatConst.SENDCHAT_HELP_ATK_CHATID_SET.contains(chatId)) {
                        if (battle.getAtkHelpChatCnt() >= 3) {
                            throw new MwException(GameError.PARAM_ERROR.getCode(), "????????????????????????????????????, roleId:", roleId,
                                    ", helpCnt:", battle.getAtkHelpChatCnt());
                        }
                        // ???????????????
                        if (chatId == ChatConst.CHAT_CAMP_ATK_SEEK_HELP && battle.getAtkLordId() != roleId) {
                            throw new MwException(GameError.PARAM_ERROR.getCode(), "????????????????????????????????????????????????, roleId:", roleId,
                                    ", helpCnt:", battle.getAtkHelpChatCnt());
                        }
                        battle.setAtkHelpChatCnt(battle.getAtkHelpChatCnt() + 1);
                        builder.setChatHelpCnt(battle.getAtkHelpChatCnt());
                    } else if (chatId == ChatConst.CHAT_SUPER_MINE_DEF_HELP
                            && battle.getType() == WorldConstant.BATTLE_TYPE_SUPER_MINE) {// ?????????????????????
                        Integer cnt = battle.getHelpChatCnt().get(roleId);
                        int chatCnt = cnt == null ? 0 : cnt.intValue(); // ???????????????????????????
                        if (chatCnt >= 3) {
                            throw new MwException(GameError.PARAM_ERROR.getCode(), "????????????????????????????????????, roleId:", roleId,
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
                throw new MwException(GameError.CHAT_CD.getCode(), "????????????????????????, roleId:", roleId, ", chatTime:",
                        player.chatTime, ", now:", now);
            }
            if (req.getChannel() == ChatConst.CHANNEL_CROSS) {
                crossChatService.shareCrossPos(player, req);
            }
        } else if (chatId == ChatConst.CHAT_HERO_SHARE || chatId == ChatConst.CHAT_MEDAL_SHARE || chatId == ChatConst.CHAT_TREASURE_WARE_SHARE) {
            //??????????????????????????????
            StaticVip staticVip = StaticVipDataMgr.getVipMap(player.lord.getVip());
            if (null == staticVip || null == staticVip.getShare() || staticVip.getShare().isEmpty()) {
                throw new MwException(GameError.VIP_CONFIG_NOT_EXIST.getCode(), "????????????vip??????????????????????????????, roleId:", player.roleId, ", chatTime:",
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
                    // ?????????????????????CD
                    wfEntity.getStatusMap().put(camp, now + WorldConstant.WAR_FIRE_CITY_HELP_CD);
                }
            }
        } else if (chatId == ChatConst.CHAT_FISHING_SHARE_FISH){
            int times = player.getFishingData().getShareTimes();
            if(times >= StaticFishMgr.getShareLogLimit()){
                throw new MwException(GameError.FISHING_SHARE_FISH_LIMIT.getCode(),GameError.err(roleId,"???????????????????????????",player.getFishingData().getShareTimes()));
            }
            times ++;
            player.getFishingData().setShareTimes(times);
            builder.setChatHelpCnt(StaticFishMgr.getShareLogLimit() - times);
        }
    }

    /**
     * ??????????????????????????????????????????
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
     * ????????????
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
            throw new MwException(GameError.CHAT_CD.getCode(), "??????????????????????????????, roleId:", roleId, ", chatTime:",
                    player.chatTime);
        }

        int keyId = 0;
        int channel = req.getChannel();

        Chat chat = null;
        if (req.hasKeyId()) {
            keyId = req.getKeyId();
            Mail mail = player.mails.get(keyId);
            if (mail == null) {
                throw new MwException(GameError.SHARE_KEY_NOT_FOUND.getCode(), "???????????????id?????????, roleId:", roleId,
                        ", keyId:", keyId);
            }
            Map<Integer, StaticMail> mailMap = StaticMailDataMgr.getMailMap();
            StaticMail staticMail = mailMap.get(mail.getMoldId());
            if (staticMail == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????mailId?????????, roleId:", roleId, ", mailId:",
                        mail.getMoldId());
            }
            int chatId = staticMail.getChatId();
            if (chatId == 0) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "?????????????????????????????????, roleId:", roleId, ", mailId:",
                        mail.getMoldId());
            }
            List<String> params = req.getParamsList();

            // if (chatId == ChatConst.CHAT_ROLE_SCOUT) { // ??????
            // CommonPb.MailScout scout = mail.getScout();
            // if (scout == null) {
            // throw new MwException(GameError.SHARE_SCOUT_NOT_EXIST.getCode(), "??????????????????????????????, roleId:", roleId,
            // ", id:", keyId);
            // }
            // params.add(player.lord.getNick());
            // params.add(mail.gettParam().get(2));
            // } else if (chatId == ChatConst.CHAT_SHARE_REPORT) { // ??????
            // CommonPb.Report report = mail.getReport();
            // if (report == null) {
            // throw new MwException(GameError.SHARE_REPORT_NOT_EXIST.getCode(), "??????????????????????????????, roleId:", roleId,
            // ", id:", keyId);
            // }
            // CommonPb.RptAtkPlayer rptPlayer = report.getRptPlayer();
            // CommonPb.RptMan attack = rptPlayer.getAttack();
            // CommonPb.RptMan defMan = rptPlayer.getDefMan();
            // params.add(attack.getName());
            // params.add(defMan.getName());
            // } else if (chatId == ChatConst.CHAT_CITY_REPORT) { // ??????
            // CommonPb.Report report = mail.getReport();
            // if (report == null) {
            // throw new MwException(GameError.SHARE_CITY_NOT_EXIST.getCode(), "??????????????????????????????, roleId:", roleId,
            // ", id:", keyId);
            // }
            // CommonPb.RptAtkPlayer rptPlayer = report.getRptPlayer();
            // CommonPb.RptMan attack = rptPlayer.getAttack();
            // CommonPb.RptCity defCity = rptPlayer.getDefCity();
            // params.add(attack.getName());
            // params.add(String.valueOf(defCity.getCityId()));
            // } else if (chatId == ChatConst.CHAT_BANDIT_REPORT) { // ??????
            // CommonPb.Report report = mail.getReport();
            // if (report == null) {
            // throw new MwException(GameError.SHARE_BANDIT_NOT_EXIST.getCode(), "??????????????????????????????, roleId:", roleId,
            // ", id:", keyId);
            // }
            // CommonPb.RptAtkBandit rptAtkBandit = report.getRptBandit();
            // CommonPb.RptBandit defend = rptAtkBandit.getDefend();
            // params.add(String.valueOf(defend.getBanditId()));
            // }
            chatDataManager.addShareReport(player, mail);
            chat = createReportShare(player, mail.getMoldId(), keyId, params, chatId);
        }

        if (channel == ChatConst.CHANNEL_WORLD) {// ??????
            chatDataManager.sendWorldChat(chat);
        } else {// ???????????????
            chatDataManager.sendCampChat(chat, player.lord.getCamp(), 0);
        }

        player.chatTime = now;

        ShareReportRs.Builder builder = ShareReportRs.newBuilder();
        return builder.build();
    }

    /**
     * ??????????????????
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
     * ????????????
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
                // ??????????????? ??????????????????
                /*if(d.getIsCampChatDia() == ChatConst.IS_CAMP_MAIL_CHAT_DIALOG) {
                    falg = false;
                	List<CommonPb.Chat> chatList = chatDataManager.getPrivateChat(roleId, d.getTargetId());
                	for(CommonPb.Chat c : chatList) {
                		if(c.getIsCampChat() != ChatConst.IS_CAMP_MAIL_CHAT) {//?????????
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
     * ????????????
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
     * ????????????
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
     * ??????????????????????????????
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
     * ????????????????????????
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
     * ??????????????????????????????
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
     * ??????????????????
     *
     * @param chat
     * @param my
     * @param targetP
     * @return ????????????????????????
     */
    private boolean sendPrivateChat(Chat chat, Player my, Player targetP) {
        CommonPb.Chat b = chatDataManager.createPrivateChat(chat, my.roleId, targetP.roleId);
        if (targetP != null && targetP.isLogin) {// ????????????
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
     * ?????????????????????????????????
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
     * ??????????????????????????????id???????????????????????????
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
     * ?????????????????????
     * 
     * @param chat
     * @param player
     * @param isLoud ?????????????????? true???????????????
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
        DataResource.sendMsgToCross(base); // ?????????????????????
    }

    /**
     * ??????????????????
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
                    iterator.remove();//?????????????????????
                }
            }
            builder.addAllChat(activityChat);
        }
        
        return builder.build();
    }
}
