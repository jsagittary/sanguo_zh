package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.List;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-03-30 16:00
 * @Description: 活动兑换表
 * @Modified By:
 */
public class StaticActExchange {

    private Integer keyId;

    // 对应s_activity_plan中的activityId
    private int activityId;
    
    //活动类型
    private int type;

    // 奖励列表
    private List<List<Integer>> awardList;

    // 兑换消耗的道具
    private List<List<Integer>> expendProp;

    // 兑换次数限制
    private Integer numberLimit;

    // 兑换等级限制
    private Integer lvLimit;

    // 描述
    private String desc;

    private int needPoint;

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

	public Integer getKeyId() {
        return keyId;
    }

    public void setKeyId(Integer keyId) {
        this.keyId = keyId;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public List<List<Integer>> getExpendProp() {
        return expendProp;
    }

    public void setExpendProp(List<List<Integer>> expendProp) {
        this.expendProp = expendProp;
    }

    public Integer getNumberLimit() {
        return numberLimit;
    }

    public void setNumberLimit(Integer numberLimit) {
        this.numberLimit = numberLimit;
    }

    public Integer getLvLimit() {
        return lvLimit;
    }

    public void setLvLimit(Integer lvLimit) {
        this.lvLimit = lvLimit;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<Integer> getProp() {
        if (!CheckNull.isEmpty(expendProp)) {
            return expendProp.get(0);
        }
        return null;
    }

    public int getNeedPoint() {
        return needPoint;
    }

    public void setNeedPoint(int needPoint) {
        this.needPoint = needPoint;
    }
}
