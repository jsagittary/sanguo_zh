package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticRedPacket.java
 * @Description 红包配置表
 * @author QiuKun
 * @date 2018年6月8日
 */
public class StaticRedPacket {

    private int id;
    private int chatId;
    private List<List<Integer>> award;// 每个红包包含的奖励内容，[[redpacket_list中的ID，个数],[[redpacket_list中的ID
    private int time;// 持续时间
    private List<Integer> message;// 留言信息Id

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public List<Integer> getMessage() {
        return message;
    }

    public void setMessage(List<Integer> message) {
        this.message = message;
    }

}
