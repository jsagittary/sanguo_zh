package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-11-24 10:24
 */
public class StaticSmallGame {
    //小游戏ID
    private int id;
    //小游戏类型
    private int type;
    //解锁条件
    private List<List<Integer>> cond;
    //通关该小游戏后可领取的奖励
    private List<List<Integer>> award;
    //可领取的额外奖励
    private List<List<Integer>> extAward;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<List<Integer>> getCond() {
        return cond;
    }

    public void setCond(List<List<Integer>> cond) {
        this.cond = cond;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public List<List<Integer>> getExtAward() {
        return extAward;
    }

    public void setExtAward(List<List<Integer>> extAward) {
        this.extAward = extAward;
    }
}
