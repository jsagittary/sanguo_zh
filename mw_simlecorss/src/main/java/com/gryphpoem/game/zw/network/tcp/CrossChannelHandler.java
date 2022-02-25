package com.gryphpoem.game.zw.network.tcp;

import com.gryphpoem.game.zw.cmd.base.Command;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.executor.ExcutorQueueType;
import com.gryphpoem.game.zw.executor.ExcutorType;
import com.gryphpoem.game.zw.mgr.CmdMgr;
import com.gryphpoem.game.zw.mgr.ExecutorPoolMgr;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.server.CrossServer;
import com.gryphpoem.game.zw.task.RecvMsgTask;

import io.netty.channel.ChannelHandlerContext;

/**
 * @ClassName CrossChannelHandler.java
 * @Description
 * @author QiuKun
 * @date 2019年4月29日
 */
public class CrossChannelHandler extends SocketChannelHandler {

    public CrossChannelHandler() {
        super("crossServer");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Base base) throws Exception {
        LogUtil.innerMessage(base);
        CmdMgr cmdMgr = CrossServer.ac.getBean(CmdMgr.class);
        Command command = cmdMgr.getCommand(base.getCmd());
        if (command == null) {
            LogUtil.error("未知协议，cmd：" + base.getCmd());
            return;
        }
        ExecutorPoolMgr.getIns().addTask(ExcutorType.LOGIC, ExcutorQueueType.LOGIC_MAIN,
                RecvMsgTask.newInstance(command, base, ctx));
    }

}
