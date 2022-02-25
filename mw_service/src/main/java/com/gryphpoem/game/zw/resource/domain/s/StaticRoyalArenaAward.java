package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * User:        zhoujie
 * Date:        2020/4/2 17:09
 * Description:
 */
public class StaticRoyalArenaAward {

    private int id;
    private int activityId;
    // 奖励档位
    private int cond;
    // 奖励类型：1为个人奖励，2为排行奖励，3为阵营奖励
    private int type;
    // 对应type为1时，领取奖励所需贡献值
    private int gloryPts;
    // 奖励
    private List<List<Integer>> reward;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getCond() {
        return cond;
    }

    public void setCond(int cond) {
        this.cond = cond;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getGloryPts() {
        return gloryPts;
    }

    public void setGloryPts(int gloryPts) {
        this.gloryPts = gloryPts;
    }

    public List<List<Integer>> getReward() {
        return reward;
    }

    public void setReward(List<List<Integer>> reward) {
        this.reward = reward;
    }
}
