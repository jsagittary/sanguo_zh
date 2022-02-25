package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * 签到数据
 */
public class SiginInfo {

    private long date;              // 签到时间
    private int level;              // 当月第一次签到时候等级
    private int times;              // 当月累计签到次数
    private int signIn;             // 当天是否已经签到 0未签到 1已经签到
    private int doubleReward;       // 当天是否已已经领取双倍奖励  0未 1已领取双倍
    private int activityId;         // 对应的活动id
    private int keyId;              // 对应的活动KeyId
    private int page;              // 签到页数，代替之前的月份

    public SiginInfo() {
    }

    public SiginInfo(CommonPb.SignInInfo s) {
        this();
        this.date = s.getDate();
        this.level = s.getLevel();
        this.times = s.getTimes();
        this.signIn = s.getSignIn();
        this.doubleReward = s.getDoubleReward();
        this.activityId = s.getActivityId();
        this.keyId = s.getKeyId();
        this.page = s.getPage();
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public int getSignIn() {
        return signIn;
    }

    public void setSignIn(int signIn) {
        this.signIn = signIn;
    }

    public int getDoubleReward() {
        return doubleReward;
    }

    public void setDoubleReward(int doubleReward) {
        this.doubleReward = doubleReward;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
