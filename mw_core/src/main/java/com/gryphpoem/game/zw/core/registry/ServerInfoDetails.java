package com.gryphpoem.game.zw.core.registry;

import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * @ClassName ServerInfoDetails.java
 * @Description 注册的服务器信息
 * @author QiuKun
 * @date 2019年5月10日
 */
@JsonRootName("details")
public class ServerInfoDetails {

    private int serverId;
    private String ip;
    private int port;
    /**
     * 服务器类型1 跨服
     */
    private int serverType;

    public ServerInfoDetails(int serverId, String ip, int port, int serverType) {
        this.serverId = serverId;
        this.ip = ip;
        this.port = port;
        this.serverType = serverType;
    }

    public ServerInfoDetails() {
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getServerType() {
        return serverType;
    }

    public void setServerType(int serverType) {
        this.serverType = serverType;
    }

}
