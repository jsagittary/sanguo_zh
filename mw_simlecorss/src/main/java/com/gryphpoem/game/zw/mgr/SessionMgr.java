package com.gryphpoem.game.zw.mgr;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.executor.ExcutorQueueType;
import com.gryphpoem.game.zw.executor.ExcutorType;
import com.gryphpoem.game.zw.network.session.SessionGroup;
import com.gryphpoem.game.zw.network.session.SocketSession;
import com.gryphpoem.game.zw.network.util.AttrKey;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.task.SendMsgTask;

import io.netty.channel.ChannelHandlerContext;

/**
 * @ClassName SessionMgr.java
 * @Description session的管理
 * @author QiuKun
 * @date 2019年5月5日
 */
@Component
public class SessionMgr {

    private static Object lock = new Object();

    /** <serverId,SessionGroup> ,游戏服的 */
    private Map<Integer, SessionGroup> sessionMap = new ConcurrentHashMap<>();

    public SessionGroup addSession(SocketSession session, int serverId, int total, int index) {
        synchronized (lock) {
            SessionGroup group = sessionMap.get(serverId);
            if (group == null) {
                group = new SessionGroup(0, serverId, total);
                sessionMap.put(serverId, group);
            }
            group.addSession(index, session);
            LogUtil.debug("serverId=" + serverId + " registed , index =" + index);
            return group;
        }
    }

    /**
     * 移除session
     * 
     * @param ctx
     */
    public void removeSession(ChannelHandlerContext ctx) {
        synchronized (lock) {
            SessionGroup group = ctx.attr(AttrKey.SESSION_KEY).get();
            if (group == null) {
                return;
            }
            Integer index = ctx.channel().attr(AttrKey.SESSION_INDEX).get();
            if (index != null) {
                group.removeSession(index);
                LogUtil.debug("serverId=" + group.getTargetServerId() + " unRegisted , index =" + index);
            }

            if (!group.isValidSession()) {
                sessionMap.remove(group.getTargetServerId());
                LogUtil.debug("serverId=" + group.getTargetServerId() + " removed");
            }
        }
    }

    /**
     * 向某个服务器返回消息
     * 
     * @param msg
     * @param serverId
     */
    public void sendMsg(Base msg, int serverId) {

        SessionGroup group = sessionMap.get(serverId);
        if (group != null) {
            ExecutorPoolMgr.getIns().addTask(ExcutorType.MSG, ExcutorQueueType.MSG_SEND,
                    SendMsgTask.newInstance(group, msg));
        }
    }

}
