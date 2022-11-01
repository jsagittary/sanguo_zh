package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @Author: GeYuanpeng
 * @Date: 2022/10/27 16:33
 */
public class StaticCharacterReward {

    private int id;

    private List<List<Integer>> need;

    private List<Integer> reward;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<List<Integer>> getNeed() {
        return need;
    }

    public void setNeed(List<List<Integer>> need) {
        this.need = need;
    }

    public List<Integer> getReward() {
        return reward;
    }

    public void setReward(List<Integer> reward) {
        this.reward = reward;
    }
}
