package com.gryphpoem.game.zw.core.common;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author xwind
 * @date 2022/5/31
 */
public class ServerInfo {
    private int serverId;
    private String serverName;
    private String environment;
    private Date openTime;

    public int getOpenServerDay() {
        LocalDate start = openTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = LocalDate.now();
        Period period = Period.between(start,end);
        return period.getDays() + 1;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Date getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Date openTime) {
        this.openTime = openTime;
    }
}
