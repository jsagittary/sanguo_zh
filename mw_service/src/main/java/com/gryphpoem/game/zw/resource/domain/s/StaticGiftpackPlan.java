package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassName StaticGiftpackPlan.java
 * @Description 礼包购买计划表
 * @author QiuKun
 * @date 2017年8月14日
 */
public class StaticGiftpackPlan {
    private int keyId;
    private int giftpackId;// 礼包的ID
    private int beginDay; // 角色创建的第几天开始
    private int endDay; // 角色创建的第几天结束
    private Date beginTime; // 礼包的开始时间
    private Date endTime; // 礼包的结束时间
    private int openBegin;// 开服第几天的
    private int openDuration;// 持续几天
    private List<List<Integer>> serverId; // 区服id

    /**
     * 该礼包是否在此时间内
     * 
     * @param openServerDay 开服时间第几天
     * @param createLordDay 创建角色第几天
     * @param nowDate 现在时间
     * @return
     */
    public boolean isThisInTime(int openServerDay, int createLordDay, Date nowDate) {
        if (beginTime != null && endTime != null) {
            return DateHelper.isInTime(nowDate, beginTime, endTime);
        } else if (beginDay > 0 && endDay > 0) {
            return createLordDay >= beginDay && createLordDay <= endDay;
        } else {
            return openServerDay >= openBegin && openServerDay <= (openBegin + openDuration);
        }
    }

    /**
     * 获取开启和结束时间
     * @param openServer 开服时间
     * @param createLord 创角时间
     * @param nowDate 现在时间
     * @return list[0]开启时间 list[1]结束时间
     */
    public List<Date> getDateList(Date openServer, Date createLord, Date nowDate) {
        Date startDate;
        Date endDate;
        if (beginDay > 0 && endDay > 0) {
            // 创角日期
            startDate = TimeHelper.getSomeDayAfterOrBerfore(createLord, beginDay - 1, 0, 0, 0);
            endDate = TimeHelper.getSomeDayAfterOrBerfore(createLord, endDay - 1, 23, 59, 59);
        } else if (openBegin > 0) {
            startDate = TimeHelper.getSomeDayAfterOrBerfore(openServer, openBegin - 1, 0, 0, 0);
            endDate = TimeHelper.getSomeDayAfterOrBerfore(startDate, openDuration - 1, 23, 59, 59);
        } else {
            startDate = beginTime;
            endDate = endTime;
        }
        return Stream.of(startDate, endDate).collect(Collectors.toList());
    }

    /**
     * 是否在时间内
     * @param openServer 开服时间
     * @param createLord 创角时间
     * @param nowDate 现在时间
     * @return true 在开启-结束时间内
     */
    public boolean isThisInTime(Date openServer, Date createLord, Date nowDate) {
        List<Date> dateList = getDateList(openServer, createLord, nowDate);
        if (CheckNull.isEmpty(dateList)) {
            // 如果配置有问题则直接过滤掉
            return false;
        }
        Date startDate = dateList.get(0);
        Date endDate = dateList.get(1);
        // 在开启和结束时间内
        return DateHelper.isInTime(nowDate, startDate, endDate);
    }

    @Deprecated
    public int getEndTimeSecond(Date openServerDate, Date createLordDate, Date nowDate) {
        if (beginTime != null && endTime != null) {
            return (int) (endTime.getTime() / 1000);
        } else if (beginDay > 0 && endDay > 0) {
            return TimeHelper.getSomeDayAfter(createLordDate, endDay - 1, 23, 59, 59);
        } else {
            return TimeHelper.getSomeDayAfter(openServerDate, openBegin + openDuration - 1, 23, 59, 59);
        }
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getGiftpackId() {
        return giftpackId;
    }

    public void setGiftpackId(int giftpackId) {
        this.giftpackId = giftpackId;
    }

    public int getBeginDay() {
        return beginDay;
    }

    public void setBeginDay(int beginDay) {
        this.beginDay = beginDay;
    }

    public int getEndDay() {
        return endDay;
    }

    public void setEndDay(int endDay) {
        this.endDay = endDay;
    }

    public int getOpenDuration() {
        return openDuration;
    }

    public void setOpenDuration(int openDuration) {
        this.openDuration = openDuration;
    }

    public List<List<Integer>> getServerId() {
        return serverId;
    }

    public void setServerId(List<List<Integer>> serverId) {
        this.serverId = serverId;
    }

}
