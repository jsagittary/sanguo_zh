package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

public class StaticGuidAward {
    private int id;
    private int cond;// 当前guideId
    private List<List<Integer>> rewards;
    private int autoNum; // 自动建造次数
    private int nextGuideId; // 需要调到下一个的guideId,填0说明没有
    private List<List<Integer>> buildIds; // 需要升级的buildingId等级 0位置建筑id,1位置建筑等级

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<List<Integer>> getRewards() {
        return rewards;
    }

    public void setRewards(List<List<Integer>> rewards) {
        this.rewards = rewards;
    }

    public int getAutoNum() {
        return autoNum;
    }

    public void setAutoNum(int autoNum) {
        this.autoNum = autoNum;
    }

    public int getCond() {
        return cond;
    }

    public void setCond(int cond) {
        this.cond = cond;
    }

    public int getNextGuideId() {
        return nextGuideId;
    }

    public void setNextGuideId(int nextGuideId) {
        this.nextGuideId = nextGuideId;
    }

    public List<List<Integer>> getBuildIds() {
        return buildIds;
    }

    public void setBuildIds(List<List<Integer>> buildIds) {
        this.buildIds = buildIds;
    }

}
