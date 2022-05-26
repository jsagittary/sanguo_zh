package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.util.CheckNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 活动计划表
 * 
 * @author tyler
 *
 */
public class StaticActivityPlan {

    private int keyId;
    private int moldId;
    private int activityType;
    private int activityId;
    private int openBegin;// 开服第几天开启 ：0无， 1 为开服第一天（当天）
    private int awardBegin;// 活动开启后多久可以领取奖励,单位 秒
    private int openDuration;// 时长: 3表示开3天
    private int displayDuration;// 活动结束后显示时长 1 表示开启时间结束后
    private int displayOpen;// 显示开启倒计时的时间
    private Integer sendMail;// 发送邮件时间  null不发  0结束后发送  1结束后1天发  适用于按开服天数开启的活动

    private Date beginTime;
    private Date endTime;
    private Date awardTime;// 可以领取奖励的时间
    private Date displayTime;// 未领取的奖励，以邮件形式发给玩家的时间
    private Date displayBegin;// 显示开启倒计时的时间

    private int plat;
    private int serverBegin;// 第几天开启 第一天填写1
    private int serverEnd;// 开服几天之内可以开
    private String name;
    private List<List<Integer>> serverId;// 服务器id
    private List<Integer> channel;
//    private String paramsStr;//活动参数
//    private List<List<Integer>> params;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getMoldId() {
        return moldId;
    }

    public void setMoldId(int moldId) {
        this.moldId = moldId;
    }

    public int getActivityType() {
        return activityType;
    }

    public void setActivityType(int activityType) {
        this.activityType = activityType;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getOpenBegin() {
        return openBegin;
    }

    public void setOpenBegin(int openBegin) {
        this.openBegin = openBegin;
    }

    public int getOpenDuration() {
        return openDuration;
    }

    public void setOpenDuration(int openDuration) {
        this.openDuration = openDuration;
    }

    public int getDisplayDuration() {
        return displayDuration;
    }

    public void setDisplayDuration(int displayDuration) {
        this.displayDuration = displayDuration;
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

    public Date getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(Date displayTime) {
        this.displayTime = displayTime;
    }

    public int getPlat() {
        return plat;
    }

    public void setPlat(int plat) {
        this.plat = plat;
    }

    public int getServerBegin() {
        return serverBegin;
    }

    public void setServerBegin(int serverBegin) {
        this.serverBegin = serverBegin;
    }

    public int getServerEnd() {
        return serverEnd;
    }

    public void setServerEnd(int serverEnd) {
        this.serverEnd = serverEnd;
    }

    public int getAwardBegin() {
        return awardBegin;
    }

    public void setAwardBegin(int awardBegin) {
        this.awardBegin = awardBegin;
    }

    public Date getAwardTime() {
        return awardTime;
    }

    public void setAwardTime(Date awardTime) {
        this.awardTime = awardTime;
    }

    public int getDisplayOpen() {
        return displayOpen;
    }

    public void setDisplayOpen(int displayOpen) {
        this.displayOpen = displayOpen;
    }

    public Integer getSendMail() {
		return sendMail;
	}

	public void setSendMail(Integer sendMail) {
		this.sendMail = sendMail;
	}

	public Date getDisplayBegin() {
        return displayBegin;
    }

    public void setDisplayBegin(Date displayBegin) {
        this.displayBegin = displayBegin;
    }

    public List<List<Integer>> getServerId() {
        return serverId;
    }

    public void setServerId(List<List<Integer>> serverId) {
        this.serverId = serverId;
    }

    public List<Integer> getChannel() {
        return CheckNull.isNull(channel) ? new ArrayList<>() : channel;
    }

    public void setChannel(List<Integer> channel) {
        this.channel = channel;
    }
}
