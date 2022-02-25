package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticChat.java
 * @Description 聊天模版信息
 * @author TanDonghai
 * @date 创建时间：2017年4月24日 下午3:55:50
 *
 */
public class StaticChat {
    private int chatId;
    private int chatType;
    private int channel;// 1 世界，2 GM，3 国家，4 私聊

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public int getChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "StaticChat [chatId=" + chatId + ", chatType=" + chatType + ", channel=" + channel + "]";
    }

}
