package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticActMonopolyCount.java
 * @Description 大富翁活动档位数据
 * @author QiuKun
 * @date 2018年9月13日
 */
public class StaticActMonopolyCount {
    private int keyId;
    private int activityId;
    private int round; // 第几轮
    private List<Integer> count;// 档位

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public List<Integer> getCount() {
        return count;
    }

    public void setCount(List<Integer> count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "StaticActMonopolyCount [keyId=" + keyId + ", activityId=" + activityId + ", round=" + round + ", count="
                + count + "]";
    }

}
