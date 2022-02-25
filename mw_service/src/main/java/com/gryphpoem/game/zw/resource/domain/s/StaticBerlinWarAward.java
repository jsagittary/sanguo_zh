package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2018-07-24 16:24
 * @description: 柏林会战奖励
 * @modified By:
 */
public class StaticBerlinWarAward {

    private int keyId;

    private int type;

    private int award;

    private List<Integer> cond;

    private List<Integer> schedule;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getAward() {
        return award;
    }

    public void setAward(int award) {
        this.award = award;
    }

    public List<Integer> getCond() {
        return cond;
    }

    public void setCond(List<Integer> cond) {
        this.cond = cond;
    }

    public List<Integer> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<Integer> schedule) {
        this.schedule = schedule;
    }
}
