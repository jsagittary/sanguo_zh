package com.gryphpoem.game.zw.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.core.util.FixSizeLinkedList;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.mgr.PlayerMgr;
import com.gryphpoem.game.zw.mgr.SessionMgr;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Chat;
import com.gryphpoem.game.zw.pb.CrossPb.SendCrossChatRq;
import com.gryphpoem.game.zw.pb.GamePb3.SyncChatRs;
import com.gryphpoem.game.zw.pb.GamePb5.*;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.util.CrossWarFinlishClear;
import com.gryphpoem.game.zw.util.PbMsgUtil;

/**
 * @ClassName CrossChatService.java
 * @Description 跨服聊天处理
 * @author QiuKun
 * @date 2019年5月13日
 */
@Component
public class CrossChatService implements CrossWarFinlishClear {

    @Autowired
    private SessionMgr sessionMgr;
    @Autowired
    private PlayerMgr playerMgr;

    /** 大喇叭的阵营Key */
    private final static int LOUD_CAMP = 0;
    /** 聊天缓存的大小 */
    private final static int CHAT_CACHE_CAPACITY = 30;
    /** 聊天的缓存 <Camp,FixSizeLinkedList> , camp=0表示大喇叭聊天 */
    private final Map<Integer, FixSizeLinkedList<CommonPb.Chat>> chatCahce = new HashMap<>(4);

    /**
     * 处理跨服消息
     * 
     * @param player
     * @param req
     */
    public void sendCrossChat(CrossPlayer player, SendCrossChatRq req) {
        Chat chat = req.getChat();
        SyncChatRs synChatRs = SyncChatRs.newBuilder().setChat(chat).build();
        final boolean isLoud = chat.getStyle() == 1;
        final int campKey = isLoud ? LOUD_CAMP : player.getCamp();
        chatCahce.computeIfAbsent(campKey, (k) -> new FixSizeLinkedList<>(CHAT_CACHE_CAPACITY)).add(chat);// 缓存消息
        // 发送消息
        playerMgr.getPlayerMap().values().stream().filter(cPlayer -> cPlayer.isFouce())
                .filter(cPlayer -> isLoud ? true : campKey == cPlayer.getCamp()).forEach(cPlayer -> {
                    Base resBase = PbMsgUtil
                            .okBase(SyncChatRs.EXT_FIELD_NUMBER, cPlayer.getLordId(), SyncChatRs.ext, synChatRs)
                            .build();
                    sessionMgr.sendMsg(resBase, cPlayer.getMainServerId());
                });
    }

    /**
     * 获取聊天消息
     * 
     * @param player
     */
    public void getCrossChat(CrossPlayer player, GetCrossChatRq req) {
        // 普通跨服聊天
        FixSizeLinkedList<Chat> normalChats = chatCahce.computeIfAbsent(player.getCamp(),
                (k) -> new FixSizeLinkedList<>(CHAT_CACHE_CAPACITY));

        // 大喇叭聊天
        FixSizeLinkedList<Chat> loudChats = chatCahce.computeIfAbsent(LOUD_CAMP,
                (k) -> new FixSizeLinkedList<>(CHAT_CACHE_CAPACITY));

        // 清除过期喇叭
        clearExceedRoleWorldChat(loudChats);
        GetCrossChatRs.Builder builder = GetCrossChatRs.newBuilder();
        builder.addAllChat(normalChats);
        builder.addAllChat(loudChats);
        Base resBase = PbMsgUtil
                .okBase(GetCrossChatRs.EXT_FIELD_NUMBER, player.getLordId(), GetCrossChatRs.ext, builder.build())
                .build();
        sessionMgr.sendMsg(resBase, player.getMainServerId());
    }

    private void clearExceedRoleWorldChat(FixSizeLinkedList<Chat> loudChats) {
        if (loudChats == null) {
            return;
        }
        int now = TimeHelper.getCurrentSecond();
        Iterator<CommonPb.Chat> iterator = loudChats.iterator();
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

    @Override
    public void clear() {
        chatCahce.clear();
        LogUtil.debug("聊天数据清除");
    }
}
