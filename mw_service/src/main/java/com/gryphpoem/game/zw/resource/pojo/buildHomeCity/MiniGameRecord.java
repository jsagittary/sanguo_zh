package com.gryphpoem.game.zw.resource.pojo.buildHomeCity;

import java.util.HashSet;
import java.util.Set;

/**
 * 小游戏记录
 *
 * @Author: GeYuanpeng
 * @Date: 2022/12/5 11:50
 */
public class MiniGameRecord {

    private int gameType; // 游戏类型

    private int gameLevel; // 已通关的最大关卡

    private Set<Integer> awardSet = new HashSet<>(); // 已经领取过的小游戏奖励

    private Set<Integer> extAwardSet = new HashSet<>(); //已经领取过的小游戏额外奖励

    public int getGameType() {
        return gameType;
    }

    public void setGameType(int gameType) {
        this.gameType = gameType;
    }

    public int getGameLevel() {
        return gameLevel;
    }

    public void setGameLevel(int gameLevel) {
        this.gameLevel = gameLevel;
    }

    public Set<Integer> getAwardSet() {
        return awardSet;
    }

    public void setAwardSet(Set<Integer> awardSet) {
        this.awardSet = awardSet;
    }

    public Set<Integer> getExtAwardSet() {
        return extAwardSet;
    }

    public void setExtAwardSet(Set<Integer> extAwardSet) {
        this.extAwardSet = extAwardSet;
    }
}
