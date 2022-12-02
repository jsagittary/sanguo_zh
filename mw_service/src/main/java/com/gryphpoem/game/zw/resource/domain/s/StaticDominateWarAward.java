package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * Description: 司隶雄踞一方奖励
 * Author: zhangpeng
 * createTime: 2022-11-24 13:40
 */
public class StaticDominateWarAward {

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
