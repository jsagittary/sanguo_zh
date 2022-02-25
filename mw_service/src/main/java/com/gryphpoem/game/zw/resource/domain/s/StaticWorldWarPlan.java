package com.gryphpoem.game.zw.resource.domain.s;

import java.util.Date;
import java.util.List;

/**
 * @ClassName StaticWorldWarPlan.java
 * @Description 世界争霸计划表
 * @author QiuKun
 * @date 2019年3月26日
 */
public class StaticWorldWarPlan {

    private int id;
    private int worldWarType;
    private Date displayBegin; // 预显示
    private Date beginTime;
    private Date endTime;
    private Date displayTime; // 结束后还可以显示的时间
    private List<List<Integer>> serverId;
    private int functionOpen;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWorldWarType() {
        return worldWarType;
    }

    public void setWorldWarType(int worldWarType) {
        this.worldWarType = worldWarType;
    }

    public Date getDisplayBegin() {
        return displayBegin;
    }

    public void setDisplayBegin(Date displayBegin) {
        this.displayBegin = displayBegin;
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

    public List<List<Integer>> getServerId() {
        return serverId;
    }

    public void setServerId(List<List<Integer>> serverId) {
        this.serverId = serverId;
    }

    public int getFunctionOpen() {
        return functionOpen;
    }

    public void setFunctionOpen(int functionOpen) {
        this.functionOpen = functionOpen;
    }

    @Override
    public String toString() {
        return "StaticWorldWarPlan [id=" + id + ", worldWarType=" + worldWarType + ", displayBegin=" + displayBegin
                + ", beginTime=" + beginTime + ", endTime=" + endTime + ", displayTime=" + displayTime + ", serverId="
                + serverId + ", functionOpen=" + functionOpen + "]";
    }

}
