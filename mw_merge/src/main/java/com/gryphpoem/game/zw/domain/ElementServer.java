package com.gryphpoem.game.zw.domain;

/**
 * @ClassName ElementServer.java
 * @Description 合服之后主服的组成元素
 * @author QiuKun
 * @date 2018年9月7日
 */
public class ElementServer implements CheckLegal {

    private int serverId;
    private int camp;

    public ElementServer() {
    }

    public ElementServer(int serverId, int camp) {
        this.serverId = serverId;
        this.camp = camp;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + camp;
        result = prime * result + serverId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ElementServer other = (ElementServer) obj;
        if (camp != other.camp) return false;
        if (serverId != other.serverId) return false;
        return true;
    }

    @Override
    public String toString() {
        return "ElementServer [serverId=" + serverId + ", camp=" + camp + "]";
    }

    @Override
    public boolean checkLegal() {
        return serverId != 0 && camp != 0;
    }
}
