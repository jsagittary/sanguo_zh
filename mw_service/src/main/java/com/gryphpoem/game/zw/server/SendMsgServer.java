package com.gryphpoem.game.zw.server;

import com.google.protobuf.Descriptors;
import com.gryphpoem.game.zw.core.net.ConnectServer;
import com.gryphpoem.game.zw.core.util.ChannelUtil;
import com.gryphpoem.game.zw.core.work.WWork;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.GamePb2;
import com.gryphpoem.game.zw.resource.domain.Msg;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;

public class SendMsgServer {


    private ConnectServer connectServer;

    /**
     * volatile修饰的内容具有可见性
     */
    private static volatile SendMsgServer ins;

    public SendMsgServer(ConnectServer connectServer) {
        this.connectServer = connectServer;
    }

    /**
     * 标准的懒汉式单例创建，双重判断避免了多线程的情况创建多个实例的情况
     *
     * @param connectServer
     * @return
     */
    public static SendMsgServer getIns(ConnectServer connectServer) {
        if (ins == null) {
            synchronized (SendMsgServer.class) {
                if (ins == null) {
                    ins = new SendMsgServer(connectServer);
                }
            }
        }
        return ins;
    }

    public static SendMsgServer getIns() {
        return ins;
    }

    public ConnectServer getConnectServer() {
        return connectServer;
    }

    public void setConnectServer(ConnectServer connectServer) {
        this.connectServer = connectServer;
    }

    public void synMsgToPlayer(Msg msg) {
        ChannelHandlerContext ctx = msg.getCtx();
        if (ctx == null) {
            return;
        }
        // 协议
        BasePb.Base base = msg.getMsg();
        // 玩家的id
        long roleId = msg.getRoleId();
        if (base.getCmd() == GamePb2.SyncChangeInfoRs.EXT_FIELD_NUMBER) {
            // 同步资源变动, 这里放入延迟执行队列
            connectServer.delayExecutor.addSyncDelayTask(ctx, roleId, base);
        } else {
            // 将任务加入执行队列
            connectServer.sendExcutor.addTask(ChannelUtil.getChannelId(ctx), new WWork(ctx, base));
            base.getAllFields().keySet()
                    .stream()
                    .map(Descriptors.FieldDescriptor::getFullName)
                    .filter(str -> !StringUtils.startsWithIgnoreCase(str, "syn") && StringUtils.endsWithIgnoreCase(str, "rs.ext"))
                    .findAny()
                    .ifPresent(ext -> {
                        // LogUtil.error("刷新延迟队列, 匹配的协议名称, name:", ext);
                        // 刷新一下延迟队列的协议
                        connectServer.delayExecutor.refreshDelayTask(roleId);
                    });

        }
    }
}
