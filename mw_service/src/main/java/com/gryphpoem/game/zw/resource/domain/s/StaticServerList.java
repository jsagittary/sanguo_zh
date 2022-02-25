package com.gryphpoem.game.zw.resource.domain.s;

import java.util.Date;

/**
 * @ClassName StaticServerList.java
 * @Description 服务器列表,用于检测s_activity_plan表使用
 * @author QiuKun
 * @date 2019年1月14日
 */
public class StaticServerList {

    private int serverId; // 服务器id
    private Date openTime;// 开服时间
    private int actMold;// 活动模板
    private String serverName;// 服务器名称

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public Date getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Date openTime) {
        this.openTime = openTime;
    }

    public int getActMold() {
        return actMold;
    }

    public void setActMold(int actMold) {
        this.actMold = actMold;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public String toString() {
        return "StaticServerList [serverId=" + serverId + ", openTime=" + openTime + ", actMold=" + actMold
                + ", serverName=" + serverName + "]";
    }

}
