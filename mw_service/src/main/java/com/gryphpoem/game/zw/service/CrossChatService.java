package com.gryphpoem.game.zw.service;

import com.alibaba.fastjson.JSONArray;
import com.gryphpoem.cross.chat.CrossChatConst;
import com.gryphpoem.cross.chat.RpcChatService;
import com.gryphpoem.cross.chat.dto.*;
import com.gryphpoem.cross.player.RpcPlayerService;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.crosssimple.util.PbCrossUtil;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.cross.StaticNewCrossDataMgr;
import com.gryphpoem.game.zw.gameplay.cross.util.CrossEntity2Dto;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.pb.GamePb7;
import com.gryphpoem.game.zw.pb.GamePb7.GetGamePlayChatRoomRs;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGamePlayPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticWarFire;
import com.gryphpoem.game.zw.resource.pojo.chat.RoleChat;
import com.gryphpoem.game.zw.resource.pojo.hero.PartnerHero;
import com.gryphpoem.game.zw.resource.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-12-06 11:07
 */
@Service
public class CrossChatService implements GmCmdService {
    @Autowired
    private RpcChatService rpcChatService;
    @Autowired
    private RpcPlayerService rpcPlayerService;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private PlayerDataManager playerDataManager;

    /**
     * 删除跨服上指定玩家的所有聊天记录
     *
     * @param player 玩家
     */
    public void deleteChatByRoleId(Player player) {
        try {
            rpcChatService.deleteChatByRoleId(player.getLordId());
        } catch (Exception e) {
            LogUtil.error(String.format("删除跨服玩家聊天数据失败!!! roleId :%d", player.getLordId()), e);
        }
    }

    /**
     * 查看玩家信息
     *
     * @param player 玩家
     * @param req
     * @return
     */
    public CompletableFuture<GamePb7.GetCrossPlayerShowRs> getCrossPlayerShow(Player player, GamePb7.GetCrossPlayerShowRq req) {
        long targetId = req.getTargetId();
        if (targetId <= 0 || targetId == player.getLordId()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(),
                    String.format("roleId: %d, 查看的玩家: %d 不存在, 或者查看的是自己本人", player.getLordId(), targetId));
        }
        Player targetPlayer = playerDataManager.getPlayer(targetId);
        if (Objects.nonNull(targetPlayer)) {//目标玩家就在本服
            CommonPb.Friend.Builder builder = CommonPb.Friend.newBuilder();
            CommonPb.Man man = PbHelper.createManPbByLord(targetPlayer);
            builder.setMan(man.toBuilder().setServerId(serverSetting.getServerID()).build());
            for (PartnerHero partnerHero : targetPlayer.getAllOnBattleHeroList()) {
                builder.addHero(partnerHero.createPb(false));
            }
            GamePb7.GetCrossPlayerShowRs.Builder rspBuilder = GamePb7.GetCrossPlayerShowRs.newBuilder();
            rspBuilder.setFriend(builder);
            GamePb7.GetCrossPlayerShowRs rsp = rspBuilder.build();
            return CompletableFuture.completedFuture(rsp);
        } else {//去跨服查找
            return rpcPlayerService.getPlayerShow(targetId).thenApply(show -> {
                GamePb7.GetCrossPlayerShowRs.Builder rsp = GamePb7.GetCrossPlayerShowRs.newBuilder();
                if (Objects.nonNull(show.getBytes())) {
                    try {
                        CommonPb.Friend friend = CommonPb.Friend.parseFrom(show.getBytes());
                        rsp.setFriend(friend);
                    } catch (Exception e) {
                        String errorMsg = String.format("lordId: %d, targetId: %d, 玩家信息解析失败!!!", player.getLordId(), targetId);
                        LogUtil.error(errorMsg, e);
                        throw new MwException(GameError.DATA_EXCEPTION.getCode(), errorMsg);
                    }
                }
                return rsp.build();
            });
        }
    }

    public CompletableFuture<GamePb7.GetGamePlayChatRoomRs> getGamePlayChatRoom(Player player, GamePb7.GetGamePlayChatRoomRq req) {
        int gamePlayPlanId = req.getGamePlayPlanId();
        int serverId = serverSetting.getServerID();
        int camp = player.getCamp();
        long lordId = player.getLordId();
        CompletableFuture<CrossPlayerChatRoom> completableFuture = rpcChatService.getGamePlayChatRoom(gamePlayPlanId, serverId, camp, lordId);
        return completableFuture.thenApply(room -> {
            GetGamePlayChatRoomRs.Builder builder = GetGamePlayChatRoomRs.newBuilder();
            builder.setRoomId(room.getRoomId());
            builder.setChlId(room.getChlId());
            builder.setMemberId(room.getMemberId());
            return builder.build();
        });
    }

    /**
     * 登录时获取聊天房间内以前的聊天记录
     *
     * @param player   玩家
     * @param roomId   房间ID
     * @param chlId    频道ID
     * @param memberId 成员ID
     * @return 回调结果
     */
    public CompletableFuture<GamePb7.GetChatRoomMsgRs> getChatRoomMsg(Player player, long roomId, int chlId, int memberId) {
        int serverId = serverSetting.getServerID();
        int camp = player.getCamp();
        long lordId = player.getLordId();
        return rpcChatService.getAllCrossChat(roomId, chlId, memberId, serverId, camp, lordId).thenApply(rtList -> {
            GamePb7.GetChatRoomMsgRs.Builder rsp = GamePb7.GetChatRoomMsgRs.newBuilder();
            if (CheckNull.nonEmpty(rtList)) {
                for (CrossChat crossChat : rtList) {
                    rsp.addChat(PbCrossUtil.buildChat(crossChat));
                }
            }
            return rsp.build();
        });
    }

    /**
     * 获取房间内其它玩家的形象信息
     *
     * @param player    玩家
     * @param roomId    玩家所在的房间ID
     * @param chlId     wanji
     * @param chatMsgId
     * @return
     * @throws MwException
     */
    public CompletableFuture<GamePb7.GetRoomPlayerShowRs> getChatRoomPlayerShow(Player player, long roomId, int chlId, long chatMsgId) {
        return rpcChatService.getPlayerShow(roomId, chlId, chatMsgId).thenApply(show -> {
            GamePb7.GetRoomPlayerShowRs.Builder rsp = GamePb7.GetRoomPlayerShowRs.newBuilder();
            if (Objects.nonNull(show.getBytes())) {
                try {
                    CommonPb.Friend friend = CommonPb.Friend.parseFrom(show.getBytes());
                    rsp.setFriend(friend);
                } catch (Exception e) {
                    String errorMsg = String.format("lordId: %d, msgId: %d, 玩家信息解析失败!!!", player.getLordId(), chatMsgId);
                    LogUtil.error(errorMsg, e);
                    throw new MwException(GameError.DATA_EXCEPTION.getCode(), errorMsg);
                }
            }
            return rsp.build();
        });
    }

    /**
     * 跨服地图中分享坐标
     *
     * @param player
     * @param req
     * @throws Exception
     */
    public void shareCrossPos(Player player, GamePb3.SendChatRq req) throws Exception {
        long roomId = req.getRoomId();
        int chlId = req.getChlId();
        int memberId = req.getMemberId();
        if (roomId <= 0 || chlId <= 0 || memberId <= 0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(),
                    String.format("roleId: %d, roomId: %d, chlId: %d, memberId: %d", player.getLordId(), roomId, chlId, memberId));
        }
        CompletableFuture<CrossChatRoom> getCrossChatRoomFuture = rpcChatService.getCrossChatRoom(serverSetting.getServerID(), player.getLordId(), roomId);
        CrossChatRoom chatRoom = getCrossChatRoomFuture.get(1, TimeUnit.SECONDS);
        StaticCrossGamePlayPlan gamePlayPlan = StaticNewCrossDataMgr.getStaticCrossGamePlayPlan((int) chatRoom.getCreatorId());
        if (Objects.isNull(gamePlayPlan)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(),
                    String.format("lordId: %d, roomId: %d, creatorId: %d, 未找到配置!!!", player.getLordId(), roomId, chatRoom.getCreatorId()));
        }
        RoleChat chat = new RoleChat();
        chat.setPlayer(player);
        chat.setMsg(String.valueOf(req.getChatId()));
        CrossRoleChat crossRoleChat = DtoParser.getCrossRoleChat(roomId, chlId, chat);
        List<String> params = new ArrayList<>(req.getContentCount() + 1);
        params.add(String.valueOf(player.lord.getArea()));
        params.addAll(req.getContentList());
        crossRoleChat.setExtParam(params);
        crossRoleChat.setMsgType(CrossChatConst.ChatMsgType.PLAYER_SHARE_POS);
        if (chatRoom.getRoomType() == CrossChatConst.ChatRoomType.GAME_PLAY_WAR_FIRE) {
            int serverId = serverSetting.getServerID();
            int forces = CrossEntity2Dto.getPlayerForce(gamePlayPlan, serverId, player.getCamp(), player.getLordId());
            crossRoleChat.setCamp(forces);
        }
        rpcChatService.sendRoleChat(memberId, crossRoleChat);
    }

    /**
     * 玩家聊天
     *
     * @param player
     * @param chat
     * @param req
     * @throws Exception
     */
    public void sendCrossRoleChat(Player player, RoleChat chat, GamePb3.SendChatRq req) throws Exception {
        long roomId = req.getRoomId();
        long lordId = player.getLordId();
        CompletableFuture<CrossChatRoom> future = rpcChatService.getCrossChatRoom(serverSetting.getServerID(), player.getLordId(), roomId);
        CrossChatRoom chatRoom = future.get(1, TimeUnit.SECONDS);
        checkCrossChatOpen(player, chatRoom);
        long nowMill = System.currentTimeMillis();
        if (chatRoom.getExpiredTime() > 0 && nowMill >= chatRoom.getExpiredTime()) {
            throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(),
                    String.format("lordId: %d, roomId: %d", lordId, roomId));
        }
        int chlId = req.getChlId();
        if (CheckNull.isEmpty(chatRoom.getChannels()) || !chatRoom.getChannels().contains(chlId)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(),
                    String.format("lordId: %d, chlId: %d, 不在房间: %d 里面!!!", lordId, chlId, roomId));
        }
        StaticCrossGamePlayPlan gamePlayPlan = StaticNewCrossDataMgr.getStaticCrossGamePlayPlan((int) chatRoom.getCreatorId());
        if (Objects.isNull(gamePlayPlan)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(),
                    String.format("lordId: %d, roomId: %d, creatorId: %d, 未找到配置!!!", lordId, roomId, chatRoom.getCreatorId()));
        }

        boolean isRoomBroadcast = req.hasStyle() && req.getStyle() == 1;
        if (isRoomBroadcast) {// 是否是大喇叭
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, PropConstant.PROP_CROSS_CHAT_ROOM_BROADCAST, 1, AwardFrom.USE_PROP);
            String[] myParam = new String[1];
            myParam[0] = String.valueOf(TimeHelper.getCurrentSecond() + Constant.PLAYER_WORLD_CHAT_SHOW_TIME);
            chat.setMyParam(myParam);
            chat.setStyle(1);
            isRoomBroadcast = true;
        }
        int memberId = req.getMemberId();
        CrossRoleChat crossRoleChat = DtoParser.getCrossRoleChat(roomId, chlId, chat);
        crossRoleChat.setMsgType(CrossChatConst.ChatMsgType.PLAYER_CHAT);
        if (chatRoom.getRoomType() == CrossChatConst.ChatRoomType.GAME_PLAY_WAR_FIRE) {
            int serverId = serverSetting.getServerID();
            int forces = CrossEntity2Dto.getPlayerForce(gamePlayPlan, serverId, player.getCamp(), lordId);
            crossRoleChat.setCamp(forces);
        }
        if (isRoomBroadcast) {
            rpcChatService.sendRoomBroadcastRoleChat(memberId, crossRoleChat);
        } else {
            rpcChatService.sendRoleChat(memberId, crossRoleChat);
        }
    }

    public void checkCrossChatOpen(Player player, CrossChatRoom room) throws MwException {
        int functionId = room.getRoomType() == CrossChatConst.ChatRoomType.GAME_PLAY_WAR_FIRE ? FunctionConstant.FUNC_CROSS_WAR_FIRE_CHAT : 0;
//        switch (room.getRoomType()) {
//            case CrossChatConst.ChatRoomType.GAME_PLAY_WAR_FIRE: {
//                functionId = FunctionConstant.FUNC_CROSS_WAR_FIRE_CHAT;
//                break;
//            }
//        }


        if (functionId <= 0) {
            throw new MwException(GameError.FUNCTION_UNLOCK_NO_CONFIG.getCode(), String.format("lordId: %d, roomId: %d, roomType: %d," +
                    " not found functionOpenId", player.getLordId(), room.getRoomId(), room.getRoomType()));
        }
        // 检测玩家进入跨服
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_CROSS_WAR_FIRE_CHAT)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "跨服战火聊天等级未开放, roleId:", player.roleId);
        }
    }

    /**
     * @param player {@link Player}
     */
    @Override
    @GmCmd(GmCmdConst.Cross.Chat.CrossChat)
    public void handleGmCmd(Player player, String... params) {
        try {
            switch (params[0]) {
                case "createWarFireChatRoom": {
                    //CrossChat createWarFireChatRoom 1300101 [[[1,28,1],[1,28,2]],[[2,29,1],[2,29,2]],[[3,28,3],[3,29,3]]]
                    int gamePlayPlanId = Integer.parseInt(params[1]);
                    Map<Integer, Map<Integer, Set<Integer>>> teamMap = parserTeamMap(gamePlayPlanId, params[2]);
                    //[[[1,28,1],[1,28,2]],[[2,29,1],[2,29,2]],[[3,28,3],[3,29,3]]]
                    rpcChatService.createWarFireChatRoom(gamePlayPlanId, teamMap, 0);
                    break;
                }
                case "sendSysMsgWarFire": {
                    //CrossChat sendSysMsgWarFire 1470242712614645762
                    long roomId = Long.parseLong(params[1]);
                    for (Map.Entry<Integer, StaticWarFire> entry : StaticCrossWorldDataMgr.getStaticWarFireMap().entrySet()) {
                        StaticWarFire swf = entry.getValue();
                        if (swf.getProtectChat() > 0) {
                            CrossSystemChat chat = new CrossSystemChat();
                            chat.setRoomId(roomId);
                            chat.setChatId(swf.getProtectChat());
                            String[] chatParams = new String[]{String.valueOf(swf.getId())};
                            chat.setParam(chatParams);
                            rpcChatService.sendGamePlaySystemChat(roomId, chat);
                        }
                        if (swf.getOccupyChat() > 0) {
                            CrossSystemChat chat = new CrossSystemChat();
                            chat.setRoomId(roomId);
                            chat.setChatId(swf.getOccupyChat());
                            String[] chatParams = new String[]{String.valueOf(swf.getId()), String.valueOf(RandomUtil.randomIntIncludeEnd(1, 3))};
                            chat.setParam(chatParams);
                            rpcChatService.sendGamePlaySystemChat(roomId, chat);
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    private Map<Integer, Map<Integer, Set<Integer>>> parserTeamMap(int gamePlayPlanId, String teamConf) throws MwException {
        JSONArray jsonArray = JSONArray.parseArray(teamConf);
        Map<Integer, Map<Integer, Set<Integer>>> teamMap = new HashMap<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray teamArray = jsonArray.getJSONArray(i);
            for (int j = 0; j < teamArray.size(); j++) {
                JSONArray serverCampArray = teamArray.getJSONArray(j);
                int teamId = serverCampArray.getIntValue(0);
                int serverId = serverCampArray.getIntValue(1);
                int camp = serverCampArray.getIntValue(2);
                Map<Integer, Set<Integer>> serverCampMap = teamMap.computeIfAbsent(teamId, k -> new HashMap<>());
                Set<Integer> camps = serverCampMap.computeIfAbsent(serverId, k -> new HashSet<>());
                camps.add(camp);
            }
        }
        if (teamMap.size() != 3) {
            throw new MwException(GameError.PARAM_ERROR.getCode(),
                    String.format("gamePlayPlanId: %d, teamConf: %s ERROR !!!", gamePlayPlanId, teamConf));
        }
        return teamMap;
    }
}
