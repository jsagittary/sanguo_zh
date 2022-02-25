package com.gryphpoem.game.zw.network.session;

import java.util.ArrayList;
import java.util.List;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BasePb.Base;

/**
 * 一个 host建立多条连接
 * 
 * @author 柳建星
 * @date 2019/01/31
 */
public class SessionGroup {

    protected SocketSession[] sessions;
    private int selfServerId;// 自身唯一标识
    private int targetServerId;// 目标唯一标识
    private String targetAddress; // 目标服务器地址

    public SessionGroup(int selfServerId, int targetServerId, int count) {
        this.selfServerId = selfServerId;
        this.targetServerId = targetServerId;
        this.sessions = new SocketSession[count];
    }

    /**
     * 判断连接是否有效
     * 
     * @param session
     * @return
     */
    public boolean isValid(SocketSession session) {
        return session != null && session.isAlive();
    }

    /**
     * 根据索引获取连接
     *
     * @param index
     * @return
     */
    public SocketSession get(int index) {
        if (index < 0 || index >= sessions.length) {
            return null;
        }
        SocketSession session = sessions[index];
        if (!isValid(session)) {
            return null;
        }
        return session;
    }

    /**
     * 获取所有连接(包括无效)
     * 
     * @return
     */
    public List<SocketSession> getAll() {
        List<SocketSession> list = new ArrayList<>();
        for (int i = 0; i < sessions.length; i++) {
            SocketSession session = sessions[i];
            list.add(session);
        }
        return list;
    }

    /**
     * 关闭所有连接
     */
    public void closeAll() {
        for (int i = 0; i < sessions.length; i++) {
            SocketSession session = sessions[i];
            if (session != null) {
                session.close();
                sessions[i] = null;
            }
        }

    }

    public void write(Base msg) {
        write(msg, msg.getLordId());
    }

    public void write(Base msg, long lordId) {
        int count = sessions.length;
        int random = 0;
        if (lordId > 0) {
            random = (int) (lordId % count);
        } else {
            random = (int) (Thread.currentThread().getId() % count);
        }
        SocketSession session = sessions[random];

        if (!isValid(session)) {
            for (SocketSession tempSession : sessions) {
                if (isValid(tempSession)) {
                    session = tempSession;
                    break;
                }
            }
            if (!isValid(session)) {
                LogUtil.error("所有 session 均断开连接");
                return;
            }
            LogUtil.error("session change : old = " + random + ", new = " + session.getIndex() + " msg = " + msg);
        }
        session.write(msg);
    }

    public synchronized void addSession(int index, SocketSession session) {
        if (index >= sessions.length) {
            LogUtil.error("index out of rang session[] , length : " + sessions.length + " , index : " + index);
            return;
        }
        if (sessions[index] != null && isValid(sessions[index])) {
            LogUtil.error("注意---- 连接 index = " + index + " 不为空,已被最新覆盖！------");
        }
        sessions[index] = session;
        LogUtil.debug("设置连接serverId=" + targetServerId + ",index = " + index);
    }

    public SocketSession removeSession(int index) {
        if (index >= sessions.length) {
            LogUtil.error("index out of rang session[] , length : " + sessions.length + " , index : " + index);
            return null;
        }
        SocketSession session = sessions[index];
        sessions[index] = null;
        LogUtil.debug("删除连接serverId=" + targetServerId + ",index = " + index);
        return session;
    }

    public SocketSession getSession(int index) {
        if (index < 0 || index >= sessions.length) {
            return null;
        }
        return sessions[index];
    }

    public boolean isValidSession() {
        for (int i = 0; i < sessions.length; i++) {
            SocketSession session = sessions[i];
            if (isValid(session)) {
                return true;
            }
        }
        return false;
    }

    protected void sendHeart(SocketSession session, int index) {

    }

    @Override
    public String toString() {
        int count = sessions == null ? 0 : sessions.length;
        StringBuilder sb = new StringBuilder();
        sb.append("serverId=").append(targetServerId).append("address=").append(targetAddress).append(",sessionCount=")
                .append(count).append(",");

        for (int i = 0; i < count; i++) {
            sb.append("index").append(i).append(" isActive=")
                    .append(sessions[i] == null ? "null" : sessions[i].isAlive());
        }
        return sb.toString();
    }

    public int getTargetServerId() {
        return targetServerId;
    }

    public int getSelfServerId() {
        return selfServerId;
    }
}
