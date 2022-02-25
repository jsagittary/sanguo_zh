package com.gryphpoem.game.zw.resource.domain.s;

import java.util.Date;
import java.util.List;

import com.gryphpoem.game.zw.resource.util.DateHelper;

public class StaticActivityTime {

    private int keyId;
    private int activityId;// 活动ID
    // private int time;// 时段。例如，1，表示这个活动的第一个时段。
    private String startTime;// 开始时间
    private String endTime;// 结束时间
    // private List<Integer> openWeekDay;// 一周内的哪几天开启
    private List<List<Integer>> awardList;// 奖励
    private List<List<Integer>> randomAward;// 奖励

    // private int startTimeSec = -1;
    // private int endTimeSec = -1;
    //
    // public int getStartTimeSec() {
    // if (startTimeSec == -1) {
    // startTimeSec = parseTime(startTime);
    // }
    // return startTimeSec + TimeHelper.getTodayZone();
    // }
    //
    // public int getEndTimeSec() {
    // if (endTimeSec == -1) {
    // endTimeSec = parseTime(endTime);
    // }
    // return endTimeSec + TimeHelper.getTodayZone();
    // }

    // private int parseTime(String time) {
    // String[] strs = time.split(":");
    // if (strs.length == 3) {
    // int second = Integer.valueOf(strs[0].trim()) * 3600;
    // second += Integer.valueOf(strs[1].trim()) * 60;
    // second += Integer.valueOf(strs[2].trim());
    // return second;
    // }
    // return -2;
    // }

    public boolean isInThisTime() {
        return DateHelper.inThisTime(new Date(), startTime, endTime);
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public List<List<Integer>> getRandomAward() {
        return randomAward;
    }

    public void setRandomAward(List<List<Integer>> randomAward) {
        this.randomAward = randomAward;
    }

    @Override
    public String toString() {
        return "StaticActivityTime [keyId=" + keyId + ", activityId=" + activityId + ", startTime=" + startTime
                + ", endTime=" + endTime + ", awardList=" + awardList + ", randomAward=" + randomAward + "]";
    }

}
