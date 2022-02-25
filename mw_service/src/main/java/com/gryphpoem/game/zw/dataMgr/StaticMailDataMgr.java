package com.gryphpoem.game.zw.dataMgr;

import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticChannelMail;
import com.gryphpoem.game.zw.resource.domain.s.StaticChat;
import com.gryphpoem.game.zw.resource.domain.s.StaticMail;
import com.gryphpoem.game.zw.resource.domain.s.StaticPushMessage;

/**
 * @author TanDonghai
 * @ClassName StaticMailDataMgr.java
 * @Description 邮件相关
 * @date 创建时间：2017年4月5日 上午10:54:30
 */
public class StaticMailDataMgr {

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    private static Map<Integer, StaticMail> mailMap;

    private static Map<Integer, StaticChat> chatMap;

    private static Map<Integer, StaticPushMessage> pushMap;

    private static List<StaticChannelMail> channelMailList;

    public static void init() {
        Map<Integer, StaticMail> mailMap = staticDataDao.selectMail();
        StaticMailDataMgr.mailMap = mailMap;

        Map<Integer, StaticChat> chatMap = staticDataDao.selectChat();
        StaticMailDataMgr.chatMap = chatMap;

        Map<Integer, StaticPushMessage> pushMap = staticDataDao.selectPushMessageMap();
        StaticMailDataMgr.pushMap = pushMap;

        List<StaticChannelMail> channelMailList = staticDataDao.selectChannelMailList();
        StaticMailDataMgr.channelMailList = channelMailList;
    }

    public static Map<Integer, StaticMail> getMailMap() {
        return mailMap;
    }

    public static StaticMail getStaticMail(int moldId) {
        return getMailMap().get(moldId);
    }

    public static Map<Integer, StaticChat> getChatMap() {
        return chatMap;
    }

    public static boolean hasChat(int chatId) {
        return chatMap.containsKey(chatId);
    }

    public static Map<Integer, StaticPushMessage> getPushMap() {
        return pushMap;
    }

    public static List<StaticChannelMail> getChannelMailList() {
        return channelMailList;
    }
}
