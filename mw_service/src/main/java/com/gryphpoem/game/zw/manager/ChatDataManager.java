package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticChatDataMgr;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.pb.GamePb3.SyncActivityChatRs;
import com.gryphpoem.game.zw.pb.GamePb3.SyncChatRs;
import com.gryphpoem.game.zw.resource.constant.ActParamConstant;
import com.gryphpoem.game.zw.resource.constant.ChatConst;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.RedPacket;
import com.gryphpoem.game.zw.resource.domain.s.StaticChat;
import com.gryphpoem.game.zw.resource.pojo.Mail;
import com.gryphpoem.game.zw.resource.pojo.chat.Chat;
import com.gryphpoem.game.zw.resource.pojo.chat.ChatDialog;
import com.gryphpoem.game.zw.resource.pojo.chat.RoleChat;
import com.gryphpoem.game.zw.resource.pojo.chat.SystemChat;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @ClassName ChatDataManager.java
 * @Description 聊天信息管理类
 * @author TanDonghai
 * @date 创建时间：2017年4月7日 上午10:30:37
 *
 */
@Component
public class ChatDataManager {

    @Autowired
    private GlobalDataManager globalDataManager;

    @Autowired
    private PlayerDataManager playerDataManager;

    // 世界聊天
    private LinkedList<CommonPb.Chat> worldChat = new LinkedList<>();
    // 大喇叭世界聊天
    private List<CommonPb.Chat> worldRoleChat;
    // 阵营、国家聊天
    private Map<Integer, LinkedList<CommonPb.Chat>> campChat;
    // 区域聊天,key为area
    private Map<Integer, LinkedList<CommonPb.Chat>> areaChat = new ConcurrentHashMap<>();

    // 分享的战报邮件
    private Map<Integer, LinkedHashMap<String, Mail>> shareReport = new ConcurrentHashMap<>();

    // 私聊的消息 key为lordId_lordId的字符串, lordId数值小的在前面
    private Map<String, LinkedList<CommonPb.Chat>> privateChat;
    // 私聊会话, key lordId
    private Map<Long, Map<Long, ChatDialog>> dialogMap;

    // key:红包唯一id
    private Map<Integer, RedPacket> redPacketMap;
    
    //活动消息
    private Map<Integer, LinkedList<CommonPb.Chat>> activityChat;

    public void init() {
        this.privateChat = globalDataManager.getGameGlobal().getPrivateChat();
        this.dialogMap = globalDataManager.getGameGlobal().getDialogMap();
        this.worldRoleChat = globalDataManager.getGameGlobal().getWorldRoleChat();
        this.redPacketMap = globalDataManager.getGameGlobal().getRedPacketMap();
        this.campChat = globalDataManager.getGameGlobal().getCampChat();
        this.areaChat = globalDataManager.getGameGlobal().getAreaChat();
        this.activityChat = globalDataManager.getGameGlobal().getActivityChat();
    }

    /**
     * 综合世界和阵营的系统消息
     * @param chatId
     * @param campOrArea 如果是本阵营通道就是 camp,本区域就是areaId
     * @param myCnt 带额外参数的
     * @param param
     */
    public Chat sendSysChat(int chatId, int campOrArea, int myCnt, Object... param) {
        StaticChat sChat = StaticChatDataMgr.getChatMapById(chatId);
        Chat chat = null;
        if (sChat != null) {
            int channel = sChat.getChannel();
            chat = myCnt > 0 ? createWithParamSysChat(chatId, myCnt, param) : createSysChat(chatId, param);
            if (ChatConst.CHANNEL_WORLD == channel) {// 世界
//                sendWorldChat(chat);
                sendCampChat(chat,1,0);
                sendCampChat(chat,2,0);
                sendCampChat(chat,3,0);
            } else if (ChatConst.CHANNEL_CAMP == channel) {// 本阵营
                sendCampChat(chat, campOrArea, 0);
            } else if (ChatConst.CHANNEL_AREA == channel) {// 本区域
                sendAreaChat(chat, campOrArea);
            } else {
                LogUtil.error("聊天配置表出错 chatId:", chatId);
            }
        } else {
            LogUtil.error("聊天配置表出错 chatId:", chatId);
        }
        return chat;
    }

    /**
     * 发送世界聊天消息
     *
     * @param chat
     */
    public void sendWorldChat(Chat chat) {
        sendWorldChat(chat, false);
    }

    /**
     * 发送世界聊天消息
     *
     * @param chat
     * @param isRole 是否是大喇叭 true大喇叭
     */
    public void sendWorldChat(Chat chat, boolean isRole) {
        CommonPb.Chat b = isRole ? addWorldRoleChat(chat) : addWorldChat(chat);
        SyncChatRs.Builder chatBuilder = SyncChatRs.newBuilder();
        chatBuilder.setChat(b);
        BasePb.Base.Builder builder = PbHelper.createSynBase(SyncChatRs.EXT_FIELD_NUMBER, SyncChatRs.ext, chatBuilder.build());
        long sendChatRoleId = 0L;
        if (!isRole && chat instanceof RoleChat) { // 黑名单不过滤大喇叭
            RoleChat rc = (RoleChat) chat;
            sendChatRoleId = rc.getPlayer().roleId;
        }
        Player player;
        Iterator<Player> it = playerDataManager.getAllOnlinePlayer().values().iterator();
        while (it.hasNext()) {
            player = it.next();
            if (player.ctx != null && !(sendChatRoleId > 0 && player.isInBlacklist(sendChatRoleId))) {// 不在黑名单中
                MsgDataManager.getIns().add(new Msg(player.ctx, builder.build(), player.roleId));
            }
        }
    }

    /**
     * 发送阵营（国家）频道消息
     *
     * @param chat
     * @param camp
     */
    public void sendCampChat(Chat chat, int camp, int area) {
        CommonPb.Chat b = null;
        if (area > 0) {
            b = addAreaChat(chat, area);
        } else {
            b = addCampChat(chat, camp);
        }

        SyncChatRs.Builder chatBuilder = SyncChatRs.newBuilder();
        chatBuilder.setChat(b);
        BasePb.Base.Builder builder = PbHelper.createSynBase(SyncChatRs.EXT_FIELD_NUMBER, SyncChatRs.ext, chatBuilder.build());
        long sendChatRoleId = 0L;
        if (chat instanceof RoleChat) {
            RoleChat rc = (RoleChat) chat;
            sendChatRoleId = rc.getPlayer().roleId;
        }
        ConcurrentHashMap<Long, Player> campMap = playerDataManager.getPlayerByCamp(camp);
        if (!CheckNull.isEmpty(campMap)) {
            Player player;
            Iterator<Player> it = campMap.values().iterator();
            while (it.hasNext()) {
                player = it.next();
                if (null != player && player.ctx != null && player.isLogin) {
                    if (area > 0 && player.lord.getArea() != area) {
                        continue;
                    }
                    if (sendChatRoleId > 0 && player.isInBlacklist(sendChatRoleId)) {// 过滤到黑名单
                        continue;
                    }
                    MsgDataManager.getIns().add(new Msg(player.ctx, builder.build(), player.roleId));
                }
            }
        }
    }

    /**
     * 发送消息到区域
     *
     * @param chat
     * @param area
     */
    private void sendAreaChat(Chat chat, int area) {
        CommonPb.Chat b = addAreaChat(chat, area);
        SyncChatRs.Builder chatBuilder = SyncChatRs.newBuilder();
        chatBuilder.setChat(b);
        BasePb.Base.Builder builder = PbHelper.createSynBase(SyncChatRs.EXT_FIELD_NUMBER, SyncChatRs.ext, chatBuilder.build());
        ConcurrentHashMap<Long, Player> players = playerDataManager.getPlayerByArea(area);
        if (!CheckNull.isEmpty(players)) {
            for (Player player : players.values()) {
                if (null != player && player.ctx != null && player.isLogin) {
                    MsgDataManager.getIns().add(new Msg(player.ctx, builder.build(), player.roleId));
                }
            }
        }
    }

    /**
     * 发送活动消息
     */
    public void sendActivityChat(int chatId, int activityId, int myCnt, Object... param) {
        StaticChat sChat = StaticChatDataMgr.getChatMapById(chatId);
        Chat chat = null;
        if (sChat != null) {
            chat = myCnt > 0 ? createWithParamSysChat(chatId, myCnt, param) : createSysChat(chatId, param);
            CommonPb.Chat b = addActivityChat(chat,activityId);
            SyncActivityChatRs.Builder chatBuilder = SyncActivityChatRs.newBuilder();
            chatBuilder.setChat(b);
            chatBuilder.setActivityId(activityId);
            BasePb.Base.Builder builder = PbHelper.createSynBase(SyncActivityChatRs.EXT_FIELD_NUMBER, SyncActivityChatRs.ext, chatBuilder.build());
            Player player;
            Iterator<Player> it = playerDataManager.getAllOnlinePlayer().values().iterator();
            while (it.hasNext()) {
                player = it.next();
                if (player.ctx != null) {
                    MsgDataManager.getIns().add(new Msg(player.ctx, builder.build(), player.roleId));
                }
            }
        } else {
            LogUtil.error("聊天配置表出错 chatId:", chatId);
        }
    }

    /**
     * 根据聊天模版id创建系统聊天对象
     *
     * @param chatId
     * @param myCnt 其他参数个数 myParam[0~myCnt] ; chat的param[myCnt~结束]
     * @param param
     * @return
     */
    private Chat createWithParamSysChat(int chatId, int myCnt, Object... param) {
        String[] params = null;
        SystemChat systemChat = new SystemChat();
        if (!CheckNull.isEmpty(param)) {
            if (myCnt > 0) {
                params = new String[myCnt];
                for (int i = 0; i < myCnt; i++) {
                    params[i] = String.valueOf(param[i]);
                }
                systemChat.setMyParam(params);
            }
            params = new String[param.length - myCnt];
            for (int i = myCnt; i < param.length; i++) {
                params[i - myCnt] = String.valueOf(param[i]);
            }
        }
        systemChat.setChatId(chatId);
        systemChat.setTime(TimeHelper.getCurrentSecond());
        systemChat.setParam(params);
        return systemChat;
    }

    /**
     * 根据聊天模版id创建系统聊天对象
     *
     * @param chatId
     * @param param
     * @return
     */
    public Chat createSysChat(int chatId, Object... param) {
        // String[] params = null;
        // if (!CheckNull.isEmpty(param)) {
        // params = new String[param.length];
        // for (int i = 0; i < param.length; i++) {
        // params[i] = String.valueOf(param[i]);
        // }
        // }
        // SystemChat systemChat = new SystemChat();
        // systemChat.setChatId(chatId);
        // systemChat.setTime(TimeHelper.getCurrentSecond());
        // systemChat.setParam(params);
        // return systemChat;
        return createWithParamSysChat(chatId, 0, param);
    }

    public List<CommonPb.Chat> getWorldRoleChat() {
        return worldRoleChat;
    }

    public CommonPb.Chat addWorldChat(Chat chat) {
        chat.setChannel(ChatConst.CHANNEL_WORLD);
        CommonPb.Chat b = chat.ser();
        worldChat.add(b);
        if (worldChat.size() > ChatConst.MAX_CHAT_COUNT) {
            worldChat.removeFirst();
        }
        return b;
    }

    public CommonPb.Chat addWorldRoleChat(Chat chat) {
        chat.setChannel(ChatConst.CHANNEL_WORLD);
        CommonPb.Chat b = chat.ser();
        worldRoleChat.add(b);
        return b;
    }

    /**
     * 清理过期世界大喇叭消息
     */
    public void clearExceedRoleWorldChat() {
        int now = TimeHelper.getCurrentSecond();
        Iterator<CommonPb.Chat> iterator = worldRoleChat.iterator();
        while (iterator.hasNext()) {
            CommonPb.Chat c = iterator.next();
            List<String> myParam = c.getMyParamList();
            String timeStr = null;
            if (!CheckNull.isEmpty(myParam)) {
                timeStr = myParam.get(0);
            }
            try {
                int time = Integer.parseInt(timeStr);
                if (now > time) {
                    iterator.remove();
                }
            } catch (Exception e) {
                iterator.remove();
            }
        }

    }

    /**
     * 添加大喇叭
     * 
     * @param chat
     * @return
     */
    public CommonPb.Chat addRoleWorldChat(Chat chat) {
        chat.setChannel(ChatConst.CHANNEL_WORLD);
        CommonPb.Chat b = chat.ser();
        worldRoleChat.add(b);
        return b;
    }

    public CommonPb.Chat addHornChat(Chat chat) {
        chat.setChannel(ChatConst.CHANNEL_WORLD);
        CommonPb.Chat b = chat.ser();
        worldChat.add(b);
        if (worldChat.size() > ChatConst.MAX_CHAT_COUNT) {
            worldChat.removeFirst();
        }
        return b;
    }

    public CommonPb.Chat addCampChat(Chat chat, int camp) {
        LinkedList<CommonPb.Chat> chatList = getCampChat(camp);
        chat.setChannel(ChatConst.CHANNEL_CAMP);
        chat.setCamp(camp);
        CommonPb.Chat b = chat.ser();
        if (b.getLordId()!=0|| ChatConst.SAVE_CHATID_LIST.contains(b.getChatId())) {
            chatList.add(b);
        }

        if (chatList.size() > ChatConst.MAX_CHAT_COUNT) {
            chatList.removeFirst();
        }
        return b;

    }

    public CommonPb.Chat addAreaChat(Chat chat, int area) {
        LinkedList<CommonPb.Chat> chatList = getAreaChat(area);

        chat.setChannel(ChatConst.CHANNEL_AREA);
        CommonPb.Chat b = chat.ser();
        chatList.add(b);

        if (chatList.size() > ChatConst.MAX_CHAT_COUNT) {
            chatList.removeFirst();
        }
        return b;
    }

    //增加活动消息
    public CommonPb.Chat addActivityChat(Chat chat, int activityId) {
        LinkedList<CommonPb.Chat> chatList = getActivityChat(activityId);
        chat.setChannel(ChatConst.CHANNEL_WORLD);
        CommonPb.Chat b = chat.ser();
        chatList.add(b);
        int maxNum = 10; //防止配置找不到占用过大内存
        List<Integer> config = ActParamConstant.ACT_NOTICE_NUM.stream().filter(conf -> conf.get(0) == activityId).findFirst().orElse(null);
        if (!CheckNull.isEmpty(config)){
            maxNum = config.get(1);
        }
        if (chatList.size() > maxNum) {
            chatList.removeFirst();
        }
        return b;
    }

    public List<CommonPb.Chat> getWorldChat() {
        return worldChat;
    }

    public LinkedList<CommonPb.Chat> getCampChat(int camp) {
        LinkedList<CommonPb.Chat> chatList = campChat.get(camp);
        if (null == chatList) {
            chatList = new LinkedList<>();
            campChat.put(camp, chatList);
        }
        return chatList;
    }

    public LinkedList<CommonPb.Chat> getAreaChat(int area) {
        LinkedList<CommonPb.Chat> chatList = areaChat.get(area);
        if (null == chatList) {
            chatList = new LinkedList<>();
            areaChat.put(area, chatList);
        }
        return chatList;
    }
    
    //获取活动消息
    public LinkedList<CommonPb.Chat> getActivityChat(int activityId) {
        LinkedList<CommonPb.Chat> chatList = activityChat.get(activityId);
        if (null == chatList) {
            chatList = new LinkedList<>();
            activityChat.put(activityId, chatList);
        }
        return chatList;
    }

    /**
     * 创建私聊消息
     * 
     * @param chat
     * @param myRoleId
     * @param targetId
     * @return
     */
    public CommonPb.Chat createPrivateChat(Chat chat, long myRoleId, long targetId) {
        CommonPb.Chat b = chat.ser();
        String dialogKey;
        // roleId数值小的在前面
        if (myRoleId > targetId) {
            dialogKey = targetId + "_" + myRoleId;
        } else {
            dialogKey = myRoleId + "_" + targetId;
        }
        LinkedList<CommonPb.Chat> chatList = privateChat.get(dialogKey);
        if (chatList == null) {
            chatList = new LinkedList<>();
            privateChat.put(dialogKey, chatList);
        }
        chatList.add(b);
        if (chatList.size() > ChatConst.MAX_CHAT_COUNT) {
            chatList.removeFirst();
        }
        // 创建会话
        Map<Long, ChatDialog> mDMap = dialogMap.get(myRoleId);
        Map<Long, ChatDialog> tDMap = dialogMap.get(targetId);
        if (mDMap == null) {
            mDMap = new HashMap<>();
            dialogMap.put(myRoleId, mDMap);
        }
        if (tDMap == null) {
            tDMap = new HashMap<>();
            dialogMap.put(targetId, tDMap);
        }

        // 自己创建会话
        ChatDialog mD = mDMap.get(targetId);
        if (mD == null) {
            mD = new ChatDialog(targetId, 0);
            mDMap.put(targetId, mD);
        }
        mD.setChat(b);// 设置最新消息
        if(b.getIsCampChat() == ChatConst.IS_CAMP_MAIL_CHAT) {
        	mD.setIsCampChatDia(ChatConst.IS_CAMP_MAIL_CHAT_DIALOG);
        }

        // 创建对方的会话
        ChatDialog tD = tDMap.get(myRoleId);
        if (tD == null) {
            tD = new ChatDialog(myRoleId, 1);
            tDMap.put(myRoleId, tD);
        }
        tD.setChat(b);// 设置最新消息
        if(b.getIsCampChat() == ChatConst.IS_CAMP_MAIL_CHAT) {
        	tD.setIsCampChatDia(ChatConst.IS_CAMP_MAIL_CHAT_DIALOG);
        }
        tD.setState(1);// 对方设置未读
        return b;
    }

    /**
     * 获取私聊信息
     * 
     * @param myRoleId
     * @param targetId
     * @return
     */
    public List<CommonPb.Chat> getPrivateChat(long myRoleId, long targetId) {
        String dialogKey;
        // roleId数值小的在前面
        if (myRoleId > targetId) {
            dialogKey = targetId + "_" + myRoleId;
        } else {
            dialogKey = myRoleId + "_" + targetId;
        }
        LinkedList<CommonPb.Chat> chatList = privateChat.get(dialogKey);
        /*LinkedList<CommonPb.Chat> chatList2 = new LinkedList<CommonPb.Chat>();
        for (CommonPb.Chat chat : chatList){
            if (chat.getLordId()==myRoleId && chat.getSystem()){
                continue;
            }
            chatList2.add(chat);
        }*/
        return chatList;
    }

    /**
     * 获取会话
     * 
     * @param myRoleId
     * @return
     */
    public Map<Long, ChatDialog> getDialog(long myRoleId) {
        return dialogMap.get(myRoleId);
    }

    /**
     * 删除会话
     * 
     * @param myRoleId
     * @param targetId
     */
    public void delDialog(long myRoleId, long targetId) {
        if (dialogMap.containsKey(myRoleId)) {
            Map<Long, ChatDialog> dMap = dialogMap.get(myRoleId);
            if (!CheckNull.isEmpty(dMap)) {
                dMap.remove(targetId);
            }
        }
    }

    /**
     * 已读会话
     * 
     * @param myRoleId
     * @param targetId
     */
    public void readDialog(long myRoleId, long targetId) {
        if (dialogMap.containsKey(myRoleId)) {
            Map<Long, ChatDialog> dMap = dialogMap.get(myRoleId);
            if (!CheckNull.isEmpty(dMap)) {
                ChatDialog chatDialog = dMap.get(targetId);
                if (chatDialog != null) {// 修改状态
                    chatDialog.setState(0);
                }
            }
        }
    }

    /**
     * 添加战报分享
     * 
     * @param player
     * @param mail
     */
    public void addShareReport(Player player, Mail mail) {
        Map<String, Mail> shareReportMail = getShareReportByCamp(player.lord.getCamp());
        StringBuffer sb = new StringBuffer();
        sb.append(player.lord.getLordId()).append("_").append(mail.getKeyId());
        String shareId = sb.toString();
        if (!shareReportMail.containsKey(shareId)) {
            shareReportMail.put(shareId, mail);
        }
        if (shareReportMail.size() > ChatConst.MAX_CHAT_COUNT) {
            for (Map.Entry<String, Mail> entry : shareReportMail.entrySet()) {
                shareReportMail.remove(entry.getKey());
                break;
            }
        }
    }

    /**
     * 通过阵营获取分享的战报信息
     * 
     * @param camp
     * @return
     */
    public Map<String, Mail> getShareReportByCamp(int camp) {
        LinkedHashMap<String, Mail> ShareReportMail = shareReport.get(camp);
        if (null == ShareReportMail) {
            ShareReportMail = new LinkedHashMap<>();
            shareReport.put(camp, ShareReportMail);
        }
        return ShareReportMail;
    }

    /**
     * 获取战报邮件
     * 
     * @param target
     * @param mailKeyId
     * @return
     */
    public Mail getShareMail(Player target, int mailKeyId) {
        Map<String, Mail> shareReportMail = getShareReportByCamp(target.lord.getCamp());
        StringBuffer sb = new StringBuffer();
        sb.append(target.lord.getLordId()).append("_").append(mailKeyId);
        String shareId = sb.toString();
        return shareReportMail.get(shareId);
    }

    public Map<Integer, RedPacket> getRedPacketMap() {
        return redPacketMap;
    }

    /**
     * 带清除过期的获取
     * 
     * @return
     */
    public Map<Integer, RedPacket> getAndClearRedPacket() {
        final int now = TimeHelper.getCurrentSecond();
        redPacketMap.values().stream().filter(rp -> rp.getExceedTime() <= now).collect(Collectors.toSet())
                .forEach(rp -> {
                    redPacketMap.remove(rp.getId());
                });
        return redPacketMap;
    }

    /**
     * 删除玩家所有聊天
     * @param lordId 玩家id
     */
    public void deleteRoleChat(long lordId) {
        // 世界聊天
        worldChat.removeIf(chat -> chat.getLordId() == lordId);
        // 大喇叭
        worldRoleChat.removeIf(chat -> chat.getLordId() == lordId);
        // 区域聊天
        areaChat.values().forEach(aChat -> aChat.removeIf(chat -> chat.getLordId() == lordId));
        // 阵营聊天
        campChat.values().forEach(cChat -> cChat.removeIf(chat -> chat.getLordId() == lordId));
        // 私聊
        privateChat.values().forEach(pChat -> pChat.removeIf(chat -> chat.getLordId() == lordId));
        // 会话删除, 删除自己的会话和其它人跟自己的会话
        dialogMap.remove(lordId);
        dialogMap.values().forEach(map -> map.remove(lordId));
        GamePb3.SyncChatMailChangeRs.Builder syncBuilder = GamePb3.SyncChatMailChangeRs.newBuilder();
        syncBuilder.addChatChange(GamePb3.SyncChatMailChangeRs.ChatChange.newBuilder().setLordId(lordId).setAction(1).build());
        BasePb.Base.Builder builder = PbHelper.createSynBase(GamePb3.SyncChatMailChangeRs.EXT_FIELD_NUMBER, GamePb3.SyncChatMailChangeRs.ext, syncBuilder.build());
        // 通知在线的客户端删除玩家的聊天记录
        playerDataManager.getAllOnlinePlayer().values().stream().filter(p -> Objects.nonNull(p.ctx))
                .forEach(p -> MsgDataManager.getIns().add(new Msg(p.ctx, builder.build(), p.roleId)));
    }

    // ===================================序列化反序列化操作====================================

}
