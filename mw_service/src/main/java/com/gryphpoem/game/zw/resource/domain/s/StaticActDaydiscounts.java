package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticActDaydiscounts.java
 * @Description 每日特惠活动
 * @author QiuKun
 * @date 2018年7月3日
 */
public class StaticActDaydiscounts {
    private int actGiftId;// 对应礼包表id
    private List<Integer> level;// 等级区间
    private int grade;// 档位值 ,用于存到 activity对象中的statusMap的key值
    private List<Integer> partyRanks;// 军阶等级
    /**
     * activityId
     */
    private int activityId;

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getActGiftId() {
        return actGiftId;
    }

    public void setActGiftId(int actGiftId) {
        this.actGiftId = actGiftId;
    }

    public List<Integer> getLevel() {
        return level;
    }

    public void setLevel(List<Integer> level) {
        this.level = level;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public List<Integer> getPartyRanks() {
        return partyRanks;
    }

    public void setPartyRanks(List<Integer> partyRanks) {
        this.partyRanks = partyRanks;
    }

    @Override
    public String toString() {
        return "StaticActDaydiscounts [actGiftId=" + actGiftId + ", level=" + level + ", grade=" + grade + "]";
    }

}
