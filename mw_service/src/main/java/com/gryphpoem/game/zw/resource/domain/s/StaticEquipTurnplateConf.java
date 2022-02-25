package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 
* @ClassName: StaticEquipTurnplateConf
* @Description: 装备转盘配置
* @author chenqi
* @date 2018年8月30日
*
 */
public class StaticEquipTurnplateConf {

    /**
     * 转盘id
     */
    private int turnplateId;
    /**
     * 活动id
     */
    private int activityId;
    /**
     * 转盘活动的贴图名称
     */
    private String bg;
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
     * 活动产出道具, 用于做保底, [[类型,Id,数量,权重,积分],……]]
     */
    private List<List<Integer>> onlyAward;
    /**
     * 实际抽取内容, [[类型,Id,数量,权重,积分],……]], 积分为可选'
     */
    private List<List<Integer>> awardList;
    /**
     * 保底, [次数下限,次数上限,奖励获得次数]
     */
    private List<Integer> probability;
    /**
     * 可替代转盘次数的物品
     */
    private List<List<Integer>> substitute;
    /**
     * 回收物品
     */
    private List<List<Integer>> returnAward;
    

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

    public List<List<Integer>> getOnlyAward() {
        return onlyAward;
    }

    public void setOnlyAward(List<List<Integer>> onlyAward) {
        this.onlyAward = onlyAward;
    }

    public String getBg() {
        return bg;
    }

    public void setBg(String bg) {
        this.bg = bg;
    }

	public List<Integer> getProbability() {
		return probability;
	}

	public void setProbability(List<Integer> probability) {
		this.probability = probability;
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
}
