package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by pengshuo on 2019/5/20 10:52
 * <br>Description: 按时触发时礼包
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class StaticGiftPackTriggerPlan {

    private int keyId;
    /** 礼包的ID */
    private int triggerId;
    /** 角色创建的第几天开始 */
    private int beginDay;
    /**
     * 角色创建结束天数
     */
    private int endDay;
    /** 礼包的开始时间*/
    private Date beginTime;
    /**
     * 礼包的结束时间
     */
    private Date endTime;
    /** 开服第几天的 */
    private int openBegin;
    /**
     * 礼包持续天数
     */
    private int openDuration;
    /** 区服id */
    private List<List<Integer>> serverId;

    /**
     * 该礼包是否在此时间内
     *
     * @param openServerDay 开服时间第几天
     * @param createLordDay 创建角色第几天
     * @param nowDate       现在时间
     * @return
     */
    public boolean isThisInTime(int openServerDay, int createLordDay, Date nowDate) {
        if (beginTime != null) {
            if (endTime != null) {
                return DateHelper.isInTime(nowDate, beginTime, endTime);
            }
            return DateHelper.isSameDate(nowDate, beginTime) && nowDate.after(beginTime);
        } else if (beginDay > 0) {
            if (endDay > 0) {
                return createLordDay >= beginDay && createLordDay <= endDay;
            }
            return createLordDay == beginDay;
        } else {
            if (openDuration >= 1) {
                int openEnd = openBegin + (openDuration - 1);
                return openServerDay >= openBegin && openServerDay <= openEnd;
            }
            return openServerDay == openBegin;
        }
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(int triggerId) {
        this.triggerId = triggerId;
    }

    public int getBeginDay() {
        return beginDay;
    }

    public void setBeginDay(int beginDay) {
        this.beginDay = beginDay;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public int getOpenBegin() {
        return openBegin;
    }

    public void setOpenBegin(int openBegin) {
        this.openBegin = openBegin;
    }

    public List<List<Integer>> getServerId() {
        return serverId;
    }

    public void setServerId(List<List<Integer>> serverId) {
        this.serverId = serverId;
    }

    /**
     * 获取礼包的结束时间
     * @param openServer 开服时间
     * @param createLord 创角时间
     * @return 礼包的结束时间
     */
    public int getGiftEndTime(Date openServer, Date createLord) {
        Date endDate = null;
        if (endDay > 0) {
            // 创角日期
            endDate = TimeHelper.getSomeDayAfterOrBerfore(createLord, endDay - 1, 23, 59, 59);
        } else if (openBegin > 0) {
            Date startDate = TimeHelper.getSomeDayAfterOrBerfore(openServer, openBegin - 1, 0, 0, 0);
            if (openDuration > 0) {
                endDate = TimeHelper.getSomeDayAfterOrBerfore(startDate, openDuration - 1, 23, 59, 59);
            }
        } else if (Objects.nonNull(endTime)) {
            endDate = endTime;
        }
        return Objects.nonNull(endDate) ? TimeHelper.dateToSecond(endDate) : 0;
    }
}
