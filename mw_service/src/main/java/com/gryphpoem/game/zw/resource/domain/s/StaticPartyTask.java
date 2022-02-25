package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 军团任务
 * 
 * @author tyler
 *
 */
public class StaticPartyTask {
    private int id;
    private int lv;
    private List<List<Integer>> award;
    private int cond;
    private int condId;
    private int schedule;
    private List<List<Integer>> extReward;
    private int extRatio;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getCond() {
        return cond;
    }

    public void setCond(int cond) {
        this.cond = cond;
    }

    public int getCondId() {
        return condId;
    }

    public void setCondId(int condId) {
        this.condId = condId;
    }

    public int getSchedule() {
        return schedule;
    }

    public void setSchedule(int schedule) {
        this.schedule = schedule;
    }

    public List<List<Integer>> getExtReward() {
        return extReward;
    }

    public void setExtReward(List<List<Integer>> extReward) {
        this.extReward = extReward;
    }

    public int getExtRatio() {
        return extRatio;
    }

    public void setExtRatio(int extRatio) {
        this.extRatio = extRatio;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

}
