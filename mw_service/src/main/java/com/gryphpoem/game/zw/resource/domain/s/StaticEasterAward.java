package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 复活节活动
 * User:        zhoujie
 * Date:        2020/4/7 14:39
 * Description:
 */
public class StaticEasterAward {

    private int keyId;
    /**
     * 活动档位
     */
    private int activityId;
    /**
     * 活动类型
     */
    private int type;
    /**
     * 充值数（钻石），满了可以开活动中的任1个
     */
    private int recharge;
    /**
     * 奖励 格式[[type,id,cnt]]
     */
    private List<List<Integer>> awardList;
    /**
     * 活动总进度
     */
    private int progress;
    /**
     * 总进度达成后的额外奖励
     */
    private List<List<Integer>> extra;
    /**
     * 档位参数
     */
    private List<Integer> param;

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRecharge() {
        return recharge;
    }

    public void setRecharge(int recharge) {
        this.recharge = recharge;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public List<List<Integer>> getExtra() {
        return extra;
    }

    public void setExtra(List<List<Integer>> extra) {
        this.extra = extra;
    }

    public List<Integer> getParam() {
        return param;
    }

    public void setParam(List<Integer> param) {
        this.param = param;
    }
}
