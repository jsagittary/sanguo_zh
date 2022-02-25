package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

public class StaticLordLv {
    private int lordLv;
    private int needExp;
    private List<List<Integer>> rewards;

    public int getLordLv() {
        return lordLv;
    }

    public List<List<Integer>> getRewards() {
        return rewards;
    }

    public void setRewards(List<List<Integer>> rewards) {
        this.rewards = rewards;
    }

    public void setLordLv(int lordLv) {
        this.lordLv = lordLv;
    }

    public int getNeedExp() {
        return needExp;
    }

    public void setNeedExp(int needExp) {
        this.needExp = needExp;
    }

}
