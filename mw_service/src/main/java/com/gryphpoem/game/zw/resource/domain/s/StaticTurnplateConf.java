package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2018-06-06 10:20
 * @description: 幸运转盘配置
 * @modified By:
 */
public class StaticTurnplateConf {

    /**
     * 转盘id
     */
    private int turnplateId;
    /**
     * 活动id
     */
    private int activityId;
    /**
     * 活动类型
     */
    private int type;
    /**
     * 免费次数 [[vip,vip,freeCnt],……]
     */
    private List<List<Integer>> freeCount;
    /**
     * 抽取次数
     */
    private int count;
    /**
     * 抽取价格
     */
    private int price;
    /**
     * 获得的积分, 可为0
     */
    private int point;
    /**
     * 实际抽取内容, [[类型,Id,数量,权重,积分],……]], 积分为可选'
     */
    private List<List<Integer>> awardList;
    /**
     * 活动产出道具, 用于做保底, [[类型,Id,数量,权重,积分],……]]
     */
    private List<List<Integer>> onlyAward;
    /**
     * 保底下限, [次数下限,次数上限,onlyAward奖励获得次数]
     */
    private List<Integer> downProbability;
    /**
     * 保底上限, [次数下限,次数上限,奖励获得次数]
     */
    private List<Integer> upProbability;
    /**
     * 特殊道具, [[类型,id],[类型,id],……]], 如果不填则无特殊道具
     */
    private List<List<Integer>> specialAward;
    /**
     * 转盘活动的贴图名称
     */
    private String bg;
    /**
     * 购买1次得1个，购买10次得10个
     */
    private List<List<Integer>> getItem;
    /**
     * 可替代转盘次数的物品
     */
    private List<List<Integer>> substitute;
    /**
     * 回收物品
     */
    private List<List<Integer>> returnAward;
    /**
     * 每日可转盘次数上限
     */
    private int dailyLimited;
    /**
     * 活动保底物品
     */
    private List<List<Integer>> probAward;
    //[[1,30,2],[31,50,1],[51,100,2],[101,999999,10]];[[1,30,2],[31,50,1],[51,100,2],[101,999999,10]]
    //[[1,30,1,1],[41,50,1,1],[51,100,1,1],[101,999999,1,1]]
    private List<List<Integer>> probList;

    public int getTurnplateId() {
        return turnplateId;
    }

    public void setTurnplateId(int turnplateId) {
        this.turnplateId = turnplateId;
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

    public List<List<Integer>> getFreeCount() {
        return freeCount;
    }

    public void setFreeCount(List<List<Integer>> freeCount) {
        this.freeCount = freeCount;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public List<List<Integer>> getSpecialAward() {
        return specialAward;
    }

    public void setSpecialAward(List<List<Integer>> specialAward) {
        this.specialAward = specialAward;
    }

    public List<List<Integer>> getOnlyAward() {
        return onlyAward;
    }

    public void setOnlyAward(List<List<Integer>> onlyAward) {
        this.onlyAward = onlyAward;
    }

    public List<Integer> getDownProbability() {
        return downProbability;
    }

    public void setDownProbability(List<Integer> downProbability) {
        this.downProbability = downProbability;
    }

    public List<Integer> getUpProbability() {
        return upProbability;
    }

    public void setUpProbability(List<Integer> upProbability) {
        this.upProbability = upProbability;
    }

    public String getBg() {
        return bg;
    }

    public void setBg(String bg) {
        this.bg = bg;
    }

    public List<List<Integer>> getGetItem() {
        return getItem;
    }

    public void setGetItem(List<List<Integer>> getItem) {
        this.getItem = getItem;
    }

    public List<List<Integer>> getSubstitute() {
        return substitute;
    }

    public void setSubstitute(List<List<Integer>> substitute) {
        this.substitute = substitute;
    }

    public List<List<Integer>> getReturnAward() {
        return returnAward;
    }

    public void setReturnAward(List<List<Integer>> returnAward) {
        this.returnAward = returnAward;
    }

    public int getDailyLimited() {
        return dailyLimited;
    }

    public void setDailyLimited(int dailyLimited) {
        this.dailyLimited = dailyLimited;
    }

    public List<List<Integer>> getProbAward() {
        return probAward;
    }

    public void setProbAward(List<List<Integer>> probAward) {
        this.probAward = probAward;
    }

    public List<List<Integer>> getProbList() {
        return probList;
    }

    public void setProbList(List<List<Integer>> probList) {
        this.probList = probList;
    }
}
