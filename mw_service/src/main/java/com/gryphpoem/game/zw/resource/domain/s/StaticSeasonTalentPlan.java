package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.util.ListUtils;

import java.util.Date;
import java.util.List;

public class StaticSeasonTalentPlan {
    private int id;
    private int planId;
    private Date beginTime;
    private Date endTime;
    private List<List<Integer>> serverId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
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

    public List<List<Integer>> getServerId() {
        return serverId;
    }

    public void setServerId(List<List<Integer>> serverId) {
        this.serverId = serverId;
    }

    public boolean isOpen() {
        long now = System.currentTimeMillis();
        return now >= this.beginTime.getTime() && now < this.getEndTime().getTime() &&
                checkServerId(this.serverId, DataResource.getBean(ServerSetting.class).getServerID());
    }

    public static boolean checkServerId(List<List<Integer>> serverIds, int checkId) {
        if(ListUtils.isNotBlank(serverIds)){
            for (List<Integer> tmp : serverIds) {
                if (checkId >= tmp.get(0) && checkId <= tmp.get(1)) {
                    return true;
                }
            }
        }
        return false;
    }
}
