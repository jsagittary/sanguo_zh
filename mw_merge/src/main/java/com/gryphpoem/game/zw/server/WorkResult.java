package com.gryphpoem.game.zw.server;

/**
 * @ClassName WorkResult.java
 * @Description
 * @author QiuKun
 * @date 2018年9月17日
 */
public class WorkResult {

    int serverId;
    Object object;
    Throwable throwable;

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

}
