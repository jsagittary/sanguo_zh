package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 需要发送广播的道具
 * @program: empire_en
 * @description:
 * @author: zhou jie
 * @create: 2020-07-23 17:48
 */
public class StaticPropChat {

    private int id;

    private int source;

    private List<Integer> item;

    private int chatId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public List<Integer> getItem() {
        return item;
    }

    public void setItem(List<Integer> item) {
        this.item = item;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }
}