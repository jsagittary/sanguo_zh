package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.Date;
import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-13 18:25
 */
public class StaticDrawHeoPlan {
    private int id;
    private Date previewTime;
    private Date beginTime;
    private Date endTime;
    private int searchTypeId;
    private List<List<Integer>> serverIdList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getSearchTypeId() {
        return searchTypeId;
    }

    public void setSearchTypeId(int searchTypeId) {
        this.searchTypeId = searchTypeId;
    }

    public List<List<Integer>> getServerIdList() {
        return serverIdList;
    }

    public void setServerIdList(List<List<Integer>> serverIdList) {
        this.serverIdList = serverIdList;
    }

    public boolean isOver(Date now) {
        if (CheckNull.isNull(this.previewTime) || CheckNull.isNull(this.beginTime) || CheckNull.isNull(this.endTime)) {
            return false;
        }
        return now.after(this.getEndTime());
    }
}
