package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 战火燎原商店
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2021-01-28 23:00
 */
public class StaticWarFireShop {

    private int id;
    private List<Integer> award;
    private int price;
    //赛季信息
    private List<List<Integer>> seasons;
    private List<Integer> schedule;
    private int num;
    /**
     * 兑换时需要走马灯，0 不需要, 1 需要
     */
    private int chat;

    /**
     * 是否跨服商品 0是非跨 1是跨
     */
    private int mold;

    /**
     * 是否需要发送跑马灯
     * @return true 需要
     */
    public boolean needSendChat() {
        return chat == 1;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getAward() {
        return award;
    }

    public void setAward(List<Integer> award) {
        this.award = award;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getChat() {
        return chat;
    }

    public void setChat(int chat) {
        this.chat = chat;
    }

    public List<List<Integer>> getSeasons() {
        return seasons;
    }

    public void setSeasons(List<List<Integer>> seasons) {
        this.seasons = seasons;
    }

    public List<Integer> getSchedule() {
        return schedule;
    }

    public boolean isMold() {
        return mold == 1;
    }

    public void setMold(int mold) {
        this.mold = mold;
    }
}