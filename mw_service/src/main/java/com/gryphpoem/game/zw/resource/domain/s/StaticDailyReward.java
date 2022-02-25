package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticDailyReward.java
 * @Description 每日登陆奖励
 * @author QiuKun
 * @date 2017年12月8日
 */
public class StaticDailyReward {
    private int id;
    private List<Integer> roleLevel;// 玩家等级范围
    private List<List<Integer>> reward;// 每日登陆邮件奖励

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getRoleLevel() {
        return roleLevel;
    }

    public void setRoleLevel(List<Integer> roleLevel) {
        this.roleLevel = roleLevel;
    }

    public List<List<Integer>> getReward() {
        return reward;
    }

    public void setReward(List<List<Integer>> reward) {
        this.reward = reward;
    }

}
