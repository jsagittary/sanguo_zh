package com.gryphpoem.game.zw.dataMgr;

import java.util.*;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.util.RandomUtil;

/**
 * @author QiuKun
 * @ClassName StaticChatDataMgr.java
 * @Description 聊天配置数据
 * @date 2017年10月24日
 */
public class StaticChatDataMgr {
    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);
    private static Map<Integer, StaticChat> chatMap;
    private static Map<Integer, StaticRedpacketList> redpacketListMap;
    private static Map<Integer, StaticRedPacket> redPacketMap;
    private static Map<Integer, StaticRedPacketMessage> redPacketMessageMap;
    private static List<StaticFriendMessage> friendMessageList;
    private static List<StaticBlackWords> blackWordsList;
    private static Map sensitiveWordMap;

    public static void init() {
        StaticChatDataMgr.chatMap = staticDataDao.selectChat();
        StaticChatDataMgr.redpacketListMap = staticDataDao.selectRedpacketList();
        StaticChatDataMgr.redPacketMap = staticDataDao.selectRedPacket();
        StaticChatDataMgr.redPacketMessageMap = staticDataDao.selectRedPacketMessage();
        StaticChatDataMgr.friendMessageList = staticDataDao.selectStaticFriendMessageList();
        StaticChatDataMgr.blackWordsList = staticDataDao.selectStaticBlackWordsList();
        try {
            //读取敏感词库
            Set set = new HashSet();
            for(StaticBlackWords words:blackWordsList){
                set.add(words.getWord());
            }
            //将敏感词库加入到HashMap中
            addSensitiveWordToHashMap(set);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Map<Integer, StaticChat> getChatMap() {
        return chatMap;
    }

    public static StaticChat getChatMapById(int chatId) {
        return chatMap.get(chatId);
    }

    public static Map<Integer, StaticRedpacketList> getRedpacketListMap() {
        return redpacketListMap;
    }

    public static StaticRedpacketList getRedpacketListById(int id) {
        return redpacketListMap.get(id);
    }

    public static Map<Integer, StaticRedPacket> getRedPacketMap() {
        return redPacketMap;
    }

    public static StaticRedPacket getRedPacketById(int id) {
        return redPacketMap.get(id);
    }

    public static Map<Integer, StaticRedPacketMessage> getRedPacketMessageMap() {
        return redPacketMessageMap;
    }

    public static String getFriendMessage(){
        StaticFriendMessage fm = RandomUtil.getListRandom(friendMessageList,1).get(0);
        return fm.getDes();
    }
    public static Map getBlackWordsMap(){
        
        return sensitiveWordMap;
    }


    /**
     * 获取手气最佳
     *
     * @param rewarPond
     * @return
     */
    public static StaticRedpacketList getRedpacketListMaxValue(List<Integer> rewarPond) {

        List<StaticRedpacketList> list = new ArrayList<>();
        rewarPond.forEach(id -> {
            list.add(redpacketListMap.get(id));
        });

        Collections.sort(list, (StaticRedpacketList o1, StaticRedpacketList o2) -> {

            if (o1.getValue() > o2.getValue()) {
                return -1;
            }
            if (o1.getValue() < o2.getValue()) {
                return 1;
            }
            return 0;
        });

        return list.get(0);
    }

    /**
     * 读取敏感词库，将敏感词放入HashSet中，构建一个DFA算法模型
     */
    private static void addSensitiveWordToHashMap(Set<String> keyWordSet) {
        sensitiveWordMap = new HashMap(keyWordSet.size());     //初始化敏感词容器，减少扩容操作
        String key = null;
        Map nowMap = null;
        Map<String, String> newWorMap = null;
        //迭代keyWordSet
        Iterator<String> iterator = keyWordSet.iterator();
        while(iterator.hasNext()){
            key = iterator.next();    //关键字
            nowMap = sensitiveWordMap;
            for(int i = 0 ; i < key.length() ; i++){
                char keyChar = key.charAt(i);       //转换成char型
                Object wordMap = nowMap.get(keyChar);       //获取

                if(wordMap != null){        //如果存在该key，直接赋值
                    nowMap = (Map) wordMap;
                }
                else{     //不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
                    newWorMap = new HashMap<String,String>();
                    newWorMap.put("isEnd", "0");     //不是最后一个
                    nowMap.put(keyChar, newWorMap);
                    nowMap = newWorMap;
                }

                if(i == key.length() - 1){
                    nowMap.put("isEnd", "1");    //最后一个
                }
            }
        }
    }


}
