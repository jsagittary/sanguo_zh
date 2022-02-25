package com.gryphpoem.game.zw.resource.domain.s;

import java.util.Date;
import java.util.List;

/**
 * @author xwind
 * @date 2021/4/16
 */
public class StaticSeasonPlan {
    private int id;
    private int season;
    private Date previewTime;
    private Date beginTime;
    private Date endTime;
    private Date displayTime;
    private List<List<Integer>> serverId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public Date getPreviewTime() {
        return previewTime;
    }

    public void setPreviewTime(Date previewTime) {
        this.previewTime = previewTime;
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
}
