package com.gryphpoem.game.zw.resource.domain.s;

import java.util.Date;
import java.util.List;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * @ClassName StaticNightRaid.java
 * @Description 夜袭功能配置
 * @author QiuKun
 * @date 2018年2月28日
 */
public class StaticNightRaid {
    private int id;
    private int openBegin; // 开服第几天开启
    private List<Integer> week;// 一周内的哪几天开启
    private String hintTimeStart;// 倒计时提示开始时间
    private String hintTimeEnd;// 倒计时提示结束时间
    private String duringTimeStart;// 开始时间
    private String duringTimeEnd;// 结束时间
    private List<Integer> banditLv;// 匪军等级限制,格式[6,7,8,11,12,13]
    private int banditCount;// 消灭匪军数量限制
    private int recoverArmy;// 恢复兵力的万分比

    /**
     * 是否在活动期间
     * 
     * @param now
     * @return true为在时间段内
     */
    public boolean isInThisTime(int now) {
        ServerSetting setting = DataResource.ac.getBean(ServerSetting.class);
        Date nowDate = new Date(now * 1000L);
        int openServerDay = setting.getOpenServerDay(nowDate);// 开服天数
        int day = TimeHelper.getCNDayOfWeek(nowDate);
        return openServerDay >= openBegin && week.contains(day)
                && DateHelper.inThisTime(nowDate, duringTimeStart, duringTimeEnd);
    }

    /**
     * 是否包含此等级的流寇
     * 
     * @param lv
     * @return true为包含
     */
    public boolean hasByBanditLv(int lv) {
        if (CheckNull.isEmpty(banditLv)) {
            return false;
        }
        return banditLv.contains(lv);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getWeek() {
        return week;
    }

    public void setWeek(List<Integer> week) {
        this.week = week;
    }

    public String getHintTimeStart() {
        return hintTimeStart;
    }

    public void setHintTimeStart(String hintTimeStart) {
        this.hintTimeStart = hintTimeStart;
    }

    public String getHintTimeEnd() {
        return hintTimeEnd;
    }

    public void setHintTimeEnd(String hintTimeEnd) {
        this.hintTimeEnd = hintTimeEnd;
    }

    public String getDuringTimeStart() {
        return duringTimeStart;
    }

    public void setDuringTimeStart(String duringTimeStart) {
        this.duringTimeStart = duringTimeStart;
    }

    public String getDuringTimeEnd() {
        return duringTimeEnd;
    }

    public void setDuringTimeEnd(String duringTimeEnd) {
        this.duringTimeEnd = duringTimeEnd;
    }

    public List<Integer> getBanditLv() {
        return banditLv;
    }

    public void setBanditLv(List<Integer> banditLv) {
        this.banditLv = banditLv;
    }

    public int getBanditCount() {
        return banditCount;
    }

    public void setBanditCount(int banditCount) {
        this.banditCount = banditCount;
    }

    public int getRecoverArmy() {
        return recoverArmy;
    }

    public void setRecoverArmy(int recoverArmy) {
        this.recoverArmy = recoverArmy;
    }

    public int getOpenBegin() {
        return openBegin;
    }

    public void setOpenBegin(int openBegin) {
        this.openBegin = openBegin;
    }

    @Override
    public String toString() {
        return "StaticNightRaid [id=" + id + ", week=" + week + ", hintTimeStart=" + hintTimeStart + ", hintTimeEnd="
                + hintTimeEnd + ", duringTimeStart=" + duringTimeStart + ", duringTimeEnd=" + duringTimeEnd
                + ", banditLv=" + banditLv + ", banditCount=" + banditCount + ", recoverArmy=" + recoverArmy + "]";
    }

}
