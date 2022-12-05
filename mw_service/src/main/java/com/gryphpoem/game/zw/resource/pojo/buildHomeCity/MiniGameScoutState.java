package com.gryphpoem.game.zw.resource.pojo.buildHomeCity;

/**
 * 前往大世界进行小游戏探索的斥候状态
 *
 * @Author: GeYuanpeng
 * @Date: 2022/12/5 10:36
 */
public class MiniGameScoutState {

    private int act; // 斥侯行动力

    private int latestRecoveryActTime; // 最近一次行动力恢复时间

    private int exploreTime; // 斥候当前剩余的探索时间, 为0则表示处于空闲

    private int miniGameId; // 探索的小游戏id

    private int miniGamePos; // 小游戏在大世界地图展示的坐标点

    public int getAct() {
        return act;
    }

    public void setAct(int act) {
        this.act = act;
    }

    public int getLatestRecoveryActTime() {
        return latestRecoveryActTime;
    }

    public void setLatestRecoveryActTime(int latestRecoveryActTime) {
        this.latestRecoveryActTime = latestRecoveryActTime;
    }

    public int getExploreTime() {
        return exploreTime;
    }

    public void setExploreTime(int exploreTime) {
        this.exploreTime = exploreTime;
    }

    public int getMiniGameId() {
        return miniGameId;
    }

    public void setMiniGameId(int miniGameId) {
        this.miniGameId = miniGameId;
    }

    public int getMiniGamePos() {
        return miniGamePos;
    }

    public void setMiniGamePos(int miniGamePos) {
        this.miniGamePos = miniGamePos;
    }
}
