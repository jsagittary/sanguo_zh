package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.Date;
import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2018-12-28 15:15
 * @description: 特殊活动
 * @modified By:
 */
public class StaticSpecialPlan {

    private int keyId;                      // key
    private int activityType;               // 活动类型
    private Date beginTime;                 // 开启时间
    private Date endTime;                   // 结束时间
    private List<List<Integer>> serverId;   // 服务器id

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getActivityType() {
        return activityType;
    }

    public void setActivityType(int activityType) {
        this.activityType = activityType;
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

    /**
     * 判断plan 是否是该服务器的
     * @param serverId
     * @return true说明是自己的服务器的plan
     */
    public boolean isSelfSeverPlan(int serverId) {
        List<List<Integer>> serverIdList = getServerId();
        if (!CheckNull.isEmpty(serverIdList) && !checkServerPlan(serverIdList, serverId)) {
            return false;
        }
        return true;
    }

    /**
     * 检测服务器id
     * @param serverIdList
     * @param serverId
     * @return
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
}
