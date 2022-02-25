package com.gryphpoem.game.zw.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.gryphpoem.game.zw.constant.MergeConstant;

/**
 * @ClassName ServerDBInfo.java
 * @Description 服务器
 * @author QiuKun
 * @date 2018年8月29日
 */
public class ServerDBInfo {
    private int serverId;
    private String dbName;
    private String dbIp;
    private int dbPort;
    private String dbUser;
    private String dbPasswd;

    /**
     * 获取DB的url
     * 
     * @return
     */
    @JSONField(serialize = false)
    public String getDBUrl() {
        return "jdbc:mysql://" + getDbIp() + ":" + getDbPort() + "/" + getDbName()
                + MergeConstant.MYSQL_PARAMS;
    }
    
    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbIp() {
        return dbIp;
    }

    public void setDbIp(String dbIp) {
        this.dbIp = dbIp;
    }

    public int getDbPort() {
        return dbPort;
    }

    public void setDbPort(int dbPort) {
        this.dbPort = dbPort;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPasswd() {
        return dbPasswd;
    }

    public void setDbPasswd(String dbPasswd) {
        this.dbPasswd = dbPasswd;
    }

    @Override
    public String toString() {
        return "ServerDBInfo [serverId=" + serverId + ", dbName=" + dbName + ", dbIp=" + dbIp + ", dbPort=" + dbPort
                + ", dbUser=" + dbUser + ", dbPasswd=" + dbPasswd + "]";
    }

}
