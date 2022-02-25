package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticActMonopoly.java
 * @Description 大富翁活动配置
 * @author QiuKun
 * @date 2018年9月11日
 */
public class StaticActMonopoly {
    private int activityId;// 活动id
    private int keyId;
    private int round; // 第几轮
    private int grid;// 格数
    private List<List<Integer>> award;// 奖励
    private int chatId;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public int getGrid() {
        return grid;
    }

    public void setGrid(int grid) {
        this.grid = grid;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    @Override
    public String toString() {
        return "StaticActMonopoly [keyId=" + keyId + ", round=" + round + ", grid=" + grid + ", award=" + award
                + ", chatId=" + chatId + "]";
    }

}
