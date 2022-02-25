package com.gryphpoem.game.zw.resource.domain;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.resource.constant.ActParamConstant;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.domain.s.StaticActAward;
import com.gryphpoem.game.zw.resource.domain.s.StaticActivity;
import com.gryphpoem.game.zw.resource.domain.s.StaticActivityPlan;
import com.gryphpoem.game.zw.resource.util.ActParamTabLoader;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.activity.ActivityAuctionService;
import org.springframework.util.ObjectUtils;

import java.util.*;

public class ActivityBase {
    private Date openTime;// 开服时间

    private StaticActivityPlan plan;// 活动计划

    private StaticActivity staticActivity;// 活动模板

    private Date beginTime; // 活动开始时间
    private Date endTime; // 活动结束时间
    private Date sendMailTime; // 发送邮件时间  
    private Date displayTime;
    private Date awardBeginTime;// 可领奖开始时间
    private Date displayOpen;// 活动显示倒计时时间

    private byte openRule = 0x0;//1：按天开放 2：按绝对时间开放

    public int getPlanKeyId() {
        return plan != null ? plan.getKeyId() : 0;
    }

    public StaticActivity getStaticActivity() {
        return staticActivity;
    }

    public void setStaticActivity(StaticActivity staticActivity) {
        this.staticActivity = staticActivity;
    }

    public StaticActivityPlan getPlan() {
        return plan;
    }


    public void setPlan(StaticActivityPlan plan) {
        this.plan = plan;
    }

    public Date getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Date openTime) {
        this.openTime = openTime;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getSendMailTime() {
        return sendMailTime;
    }

    public void setSendMailTime(Date sendMailTime) {
        this.sendMailTime = sendMailTime;
    }

    public Date getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(Date displayTime) {
        this.displayTime = displayTime;
    }

    public int getActivityId() {
        return this.plan.getActivityId();
    }

    public int getActivityType() {
        return this.plan.getActivityType();
    }

    public int getDayiy() {
        return DateHelper.dayiy(openTime, new Date());
    }

    /**
     * 通过活动开始时间计时
     *
     * @return
     */
    public int getDayiyBegin() {
        return DateHelper.dayiy(beginTime, new Date());
    }

    public Date getAwardBeginTime() {
        return awardBeginTime;
    }

    public void setAwardBeginTime(Date awardBeginTime) {
        this.awardBeginTime = awardBeginTime;
    }

    public Date getDisplayOpen() {
        return displayOpen;
    }

    public void setDisplayOpen(Date displayOpen) {
        this.displayOpen = displayOpen;
    }

    /**
     * 是否在领奖时间区间内
     *
     * @return true 在领奖区间内
     */
    public boolean isReceiveAwardTime() {
        long now = System.currentTimeMillis();
        long end = endTime.getTime();
        long award = awardBeginTime.getTime();
        return now >= award && now <= end;
    }

    /**
     * 判断plan 是否是该服务器的
     *
     * @param serverId
     * @return true说明是自己的服务器的plan
     */
    /*public boolean isSelfSeverPlan(int serverId) {
        List<Integer> serverIdList = plan.getServerId();
        if (!CheckNull.isEmpty(serverIdList) && !serverIdList.contains(serverId)) {
            return false;
        }
        int beginLimit = plan.getServerBegin();// 活动开启限制
        int endLimit = plan.getServerEnd();// 活动结束限制
        if (beginLimit != 0 && endLimit != 0) {
            int openBegin = plan.getOpenBegin();
            if (openBegin > 0) {
                if (openBegin < beginLimit || openBegin > endLimit) {
                    return false;
                }
            } else {
                int dayiy = DateHelper.dayiy(openTime, beginTime);// 开服时间距活动开启天数
                if (beginLimit != 0 && endLimit != 0 && (dayiy < beginLimit || dayiy > endLimit)) {// 活动开启限制
                    return false;
                }
            }
        }
        return true;
    }*/

    /**
     * 判断plan 是否是该服务器的
     *
     * @param serverId
     * @return true说明是自己的服务器的plan
     */
    public boolean isSelfSeverPlan(int serverId) {
        List<List<Integer>> serverIdList = plan.getServerId();
        if (!CheckNull.isEmpty(serverIdList) && !checkServerPlan(serverIdList, serverId)) {
            return false;
        }
        int beginLimit = plan.getServerBegin();// 活动开启限制
        int endLimit = plan.getServerEnd();// 活动结束限制
        if (beginLimit != 0 && endLimit != 0) {
            int openBegin = plan.getOpenBegin();
            if (openBegin > 0) {
                if (openBegin < beginLimit || openBegin > endLimit) {
                    return false;
                }
            } else {
                int dayiy = DateHelper.dayiy(openTime, beginTime);// 开服时间距活动开启天数
                if (beginLimit != 0 && endLimit != 0 && (dayiy < beginLimit || dayiy > endLimit)) {// 活动开启限制
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param @param  serverIdList 配置的区服id
     * @param @param  serverId  当前服
     * @param @return 参数
     * @return boolean    返回类型  true 包含
     * @throws
     * @Title: checkServerPlan
     * @Description: (判断配置的活动区服id中是否包含当前区服)
     */
    public boolean checkServerPlan(List<List<Integer>> serverIdList, int serverId) {
        for (List<Integer> list : serverIdList) {
            //获取起始id和结束id
            if (list.size() >= 2) {
                int startServerId = list.get(0);
                int endServerId = list.get(1);
                if (startServerId <= endServerId && serverId >= startServerId && serverId <= endServerId) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 初始化一些活动时间数据 根据时间判断是否加入到活动列表中
     *
     * @return true加入活动列表
     */
    /*public boolean initData() {
        int beginDate = plan.getOpenBegin();// 开服的第几天
        if (beginDate != 0) { // 按照开服第几天时间计算
            int awardBeginDate = plan.getAwardBegin();// 可领取奖励的开始时间,单位秒
            int openDuration = plan.getOpenDuration();//活动时长
            if (openDuration == 0) {
                return false;
            }
            int displayDuration = plan.getDisplayDuration();//活动结束后显示时长 1 表示开启时间结束后
            int displayOpen = plan.getDisplayOpen();//显示开启倒计时的时间，适用于按开服天数开启的活动
            Integer sendMail = plan.getSendMail();//发送邮件时间  -1不发 0结束后发送  1结束后1天发  适用于按开服天数开启的活动
            Calendar open = Calendar.getInstance();
            open.setTime(openTime);
            open.set(Calendar.HOUR_OF_DAY, 0);
            open.set(Calendar.MINUTE, 0);
            open.set(Calendar.SECOND, 0);
            open.set(Calendar.MILLISECOND, 0);
            long openMillis = open.getTimeInMillis();

            //获取活动开启时间   = （开服后的天数-1） + 开服时间
            long beginMillis = (beginDate - 1) * 24 * 3600 * 1000L + openMillis;
            this.beginTime = new Date(beginMillis);

            // 等级活动和投资计划时间必须按之前设定的做处理
            // int activityId = this.staticActivity.getType();
            // if (activityId == 1
            // || activityId == 15
            // || activityId == 18
            // || activityId == ActivityConst.ACT_INVEST_NEW
            // || activityId == ActivityConst.ACT_VIP_GIFT) {
            // this.beginTime = DateHelper.parseDate("2015-11-17 00:00:00");
            // }

            // 可领取奖励开始时间  = 活动开启时间 + 可领取奖励的时间
            long awardBeginMillis = beginMillis + awardBeginDate * 1000L;
            this.awardBeginTime = new Date(awardBeginMillis);

            //活动结束时间  = 开始时间 + （活动时长 - 1）
            long endMillis = beginMillis + openDuration * 24 * 3600 * 1000L - 1;
            this.endTime = new Date(endMillis);

            //显示时长 = 活动结束时间  + 显示时长
            if (displayDuration != 0) {
                long displayMillis = endMillis + displayDuration * 24 * 3600 * 1000L;
                this.displayTime = new Date(displayMillis);
            }
            
            //发送邮件的时间  支持活动结束后立即发 0活动结束后立即发  1结束时间1天后发  以此类推
            if(sendMail != null) {
            	long displayMillis = endMillis + sendMail * 24 * 3600 * 1000L;
                this.sendMailTime = new Date(displayMillis);
            }
            
            //活动开启倒计时时间 = （倒计时时间 -1） + 开服时间
            if (displayOpen != 0) {
                long displayOpenMillis = (displayOpen - 1) * 24 * 3600 * 1000L + openMillis;
                this.displayOpen = new Date(displayOpenMillis);
            }
            return true;

        } else {// 自定义开始和结束时间
            if (plan.getBeginTime() == null || plan.getEndTime() == null) {
                return false;
            }

            this.beginTime = plan.getBeginTime();
            this.endTime = plan.getEndTime();
            this.sendMailTime = plan.getDisplayTime();
            Date awardTime = plan.getAwardTime();//可领取奖励时间
            this.awardBeginTime = awardTime == null ? this.beginTime : awardTime;
            Date displayBegin = plan.getDisplayBegin();//显示开启倒计时的时间，适用于按日期开放的活动
            this.displayOpen = displayBegin == null ? this.beginTime : displayBegin;
            return true;
        }
    }*/


    /**
     * 初始化一些活动时间数据 根据时间判断是否加入到活动列表中
     *
     * @return true加入活动列表
     */
    public boolean initData() {
        int beginDate = plan.getOpenBegin();// 开服的第几天
        if (beginDate != 0) { // 按照开服第几天时间计算
            int awardBeginDate = plan.getAwardBegin();// 可领取奖励的开始时间,单位秒
            int openDuration = plan.getOpenDuration();//活动时长
            if (openDuration == 0) {
                return false;
            }
            int displayDuration = plan.getDisplayDuration();//活动结束后显示时长 1 表示开启时间结束后
            int displayOpen = plan.getDisplayOpen();//显示开启倒计时的时间，适用于按开服天数开启的活动
            Integer sendMail = plan.getSendMail();//发送邮件时间  -1不发 0结束后发送  1结束后1天发  适用于按开服天数开启的活动
            Calendar open = Calendar.getInstance();
            open.setTime(openTime);
            open.set(Calendar.HOUR_OF_DAY, 0);
            open.set(Calendar.MINUTE, 0);
            open.set(Calendar.SECOND, 0);
            open.set(Calendar.MILLISECOND, 0);
            long openMillis = open.getTimeInMillis();

            //获取活动开启时间   = （开服后的天数-1） + 开服时间
            this.beginTime = TimeHelper.getSomeDayAfterOrBerfore(openTime, beginDate - 1, 0, 0, 0);
            long beginMillis = beginTime.getTime();

            // 可领取奖励开始时间  = 活动开启时间 + 可领取奖励的时间
            long awardBeginMillis = beginMillis + awardBeginDate * 1000L;
            this.awardBeginTime = new Date(awardBeginMillis);


            //活动结束时间  = 开始时间 + （活动时长 - 1）
            this.endTime = TimeHelper.getSomeDayAfterOrBerfore(beginTime, openDuration - 1, 23, 59, 59);

            //显示时长 = 活动结束时间  + 显示时长
            if (displayDuration != 0) {
                this.displayTime = TimeHelper.getSomeDayAfterOrBerfore(endTime, displayDuration, 23, 59, 59);
            } else {
                this.displayTime = this.endTime;
            }

            //发送邮件的时间  支持活动结束后立即发 0活动结束后立即发  1结束时间1天后发  以此类推
            if (sendMail != null) {
                this.sendMailTime = TimeHelper.getSomeDayAfterOrBerfore(endTime, sendMail, 23, 59, 59);
            }

            //活动开启倒计时时间 = （倒计时时间 -1） + 开服时间
            if (displayOpen != 0) {
                this.displayOpen = TimeHelper.getSomeDayAfterOrBerfore(openTime, displayOpen - 1, 0, 0, 0);
                // long displayOpenMillis = (displayOpen - 1) * 24 * 3600 * 1000L + openMillis;
                // this.displayOpen = new Date(displayOpenMillis);
            }
            // return true;
            this.setOpenRule((byte) 0x1);
        } else {// 自定义开始和结束时间
            if (plan.getBeginTime() == null || plan.getEndTime() == null) {
                return false;
            }

            this.beginTime = plan.getBeginTime();
            this.endTime = plan.getEndTime();
            if (getActivityType() == ActivityConst.ACT_CHRISTMAS//圣诞活动配置绝对时间，需要再endTime时发积分奖励，未领取的奖励也在此刻处理
                    || getActivityType() == ActivityConst.ACT_REPAIR_CASTLE) {//修缮城堡活动
                this.sendMailTime = plan.getEndTime();
            } else {
                //这里做一个容错处理，原来是需要配置displayTIme才会发，策划经常性的不配displayTime导致活动在结束时间没有回收
                //why ? 因为在添加活动job时有个条件，如果sendMailTime是空就跳过
                this.sendMailTime = Objects.isNull(plan.getDisplayTime()) ? plan.getEndTime() : plan.getDisplayTime();
            }
            Date awardTime = plan.getAwardTime();//可领取奖励时间
            this.awardBeginTime = awardTime == null ? this.beginTime : awardTime;
            Date displayBegin = plan.getDisplayBegin();//显示开启倒计时的时间，适用于按日期开放的活动
            this.displayOpen = displayBegin == null ? this.beginTime : displayBegin;
            this.displayTime = plan.getDisplayTime();
            // return true;
            this.setOpenRule((byte) 0x2);
        }
        // 勇冠三军的特殊处理
        if (ActivityConst.ACT_BRAVEST_ARMY == this.getActivityType()) {
            this.displayTime = (TimeHelper.getSomeDayAfterOrBerfore(this.endTime, 1, 23, 59, 59));
        }

        return true;
    }

    /**
     * 活动
     *
     * @return
     */
/*    public int getStep() {
        int openBegin = plan.getOpenBegin();
        int openDuration = plan.getOpenDuration();
        int displayDuration = plan.getDisplayDuration();
        Date now = new Date();// 当前时间

        if (openBegin > 0) {// 开服计划类活动跟着顺序走
            int dayiy = DateHelper.dayiy(openTime, now);// 开服距当前天数

            int endDate = Math.addExact(openBegin, openDuration);
            int displayDate = Math.addExact(endDate, displayDuration);
            int beginLimit = plan.getServerBegin();// 活动开启限制
            int endLimit = plan.getServerEnd();// 活动结束限制

            if (beginLimit != 0 && endLimit != 0 && (openBegin < beginLimit || openBegin > endLimit)) {// 活动开启限制
                return ActivityConst.OPEN_CLOSE;
            }
            
            if (dayiy >= endDate && dayiy < displayDate) {
                return ActivityConst.OPEN_AWARD;
            }

            if (dayiy >= openBegin && dayiy < endDate) {
                return ActivityConst.OPEN_STEP;
            }
        } else {// 具体的时间段 常规,促销,全服性质活动
            Date beginTime = plan.getBeginTime();
            Date endTime = plan.getEndTime();
            Date displayTime = plan.getDisplayTime();

            if (beginTime == null || endTime == null) {
                return ActivityConst.OPEN_CLOSE;
            }

            int beginLimit = plan.getServerBegin();// 活动开启限制
            int endLimit = plan.getServerEnd();// 活动结束限制

            int dayiy = DateHelper.dayiy(openTime, beginTime);// 开服时间距活动开启天数
            
            
            if (beginLimit != 0 && endLimit != 0 && (dayiy < beginLimit || dayiy > endLimit)) {// 活动开启限制
                return ActivityConst.OPEN_CLOSE;
            }
            
            if (displayTime != null && now.after(endTime) && now.before(displayTime)) {
                return ActivityConst.OPEN_AWARD;
            }
            
            if (now.after(beginTime) && now.before(endTime)) {
                return ActivityConst.OPEN_STEP;
            }
        }
        return ActivityConst.OPEN_CLOSE;
    }*/
    public int getStep0() {
        int openBegin = plan.getOpenBegin();
        Date now = new Date();
        if (openBegin > 0) {
            if (this.beginTime == null || this.endTime == null) {
                return ActivityConst.OPEN_CLOSE;
            }
            if (this.displayOpen != null && now.after(this.displayOpen) && now.before(this.beginTime)) {
                return ActivityConst.DISPLAY_OPEN;
            }
            if (now.after(this.beginTime) && now.before(this.endTime)) {
                return ActivityConst.OPEN_STEP;
            }
            if (now.after(this.endTime) && this.displayTime != null && now.before(this.displayTime)) {
                return ActivityConst.OPEN_AWARD;
            }
        } else {
            Date beginTime = plan.getBeginTime();
            Date endTime = plan.getEndTime();
            Date displayTime = plan.getDisplayTime();
            Date displayBegin = plan.getDisplayBegin();
            if (beginTime == null || endTime == null) {
                return ActivityConst.OPEN_CLOSE;
            }
            int beginLimit = plan.getServerBegin();// 活动开启限制
            int endLimit = plan.getServerEnd();// 活动结束限制

            int dayiy = DateHelper.dayiy(openTime, beginTime);// 开服时间距活动开启天数

            if (beginLimit != 0 && endLimit != 0 && (dayiy < beginLimit || dayiy > endLimit)) {// 活动开启限制
                return ActivityConst.OPEN_CLOSE;
            }
            if (displayBegin != null && now.after(displayBegin) && now.before(beginTime)) {
                return ActivityConst.DISPLAY_OPEN;
            }
            if (now.after(beginTime) && now.before(endTime)) {
                return ActivityConst.OPEN_STEP;
            }
            if (now.after(endTime) && displayTime != null && now.before(displayTime)) {
                return ActivityConst.OPEN_AWARD;
            }
        }
        return ActivityConst.OPEN_CLOSE;
    }

    public int getStep() {
        int openBegin = plan.getOpenBegin();
        int openDuration = plan.getOpenDuration();
        int displayDuration = plan.getDisplayDuration();
        Date now = new Date();// 当前时间

        if (openBegin > 0) {// 开服计划类活动跟着顺序走

            // 过了结束时间, 或者没到开启时间
            if (this.beginTime == null || this.endTime == null) {
                return ActivityConst.OPEN_CLOSE;
            }
            if (now.after(this.endTime) && this.displayTime != null && now.before(this.displayTime)) {
                return ActivityConst.OPEN_AWARD;
            }
            if (now.after(this.beginTime) && now.before(this.endTime)) {
                return ActivityConst.OPEN_STEP;
            }
        } else {// 具体的时间段 常规,促销,全服性质活动
            Date beginTime = plan.getBeginTime();
            Date endTime = plan.getEndTime();
            Date displayTime = plan.getDisplayTime();

            if (beginTime == null || endTime == null) {
                return ActivityConst.OPEN_CLOSE;
            }

            int beginLimit = plan.getServerBegin();// 活动开启限制
            int endLimit = plan.getServerEnd();// 活动结束限制

            int dayiy = DateHelper.dayiy(openTime, beginTime);// 开服时间距活动开启天数


            if (beginLimit != 0 && endLimit != 0 && (dayiy < beginLimit || dayiy > endLimit)) {// 活动开启限制
                return ActivityConst.OPEN_CLOSE;
            }

            if (displayTime != null && now.after(endTime) && now.before(displayTime)) {
                return ActivityConst.OPEN_AWARD;
            }

            if (now.after(beginTime) && now.before(endTime)) {
                return ActivityConst.OPEN_STEP;
            }
        }
        return ActivityConst.OPEN_CLOSE;
    }

    /**
     * 获取活动状态,目前此方法只是为了兼容原来的
     *
     * @return
     */
    public int getBaseOpen() {
        return getStep();
    }

    /**
     * 活动是否在DISPLAY-OPEN之间
     *
     * @return
     */
    public boolean isBaseDisplay() {
        Date now = new Date();// 当前时间
        if (!CheckNull.isNull(displayOpen) && DateHelper.isAfterTime(beginTime, now) && DateHelper.isAfterTime(now, displayOpen) && !DateHelper.isAfterTime(now, endTime)) {
            return true;
        }
        return false;
    }

    public byte getOpenRule() {
        return openRule;
    }

    public void setOpenRule(byte openRule) {
        this.openRule = openRule;
    }

}
