package com.gryphpoem.game.zw.core.work;

import com.gryphpoem.game.zw.core.handler.AbsInnerHandler;
import com.gryphpoem.game.zw.core.message.MessagePool;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.handler.inner.ClientRoutHandler;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.server.AppGameServer;

import io.netty.channel.ChannelHandlerContext;

/**
 * 跨服之间传输的数据传输
 *
 */
public class IRWork extends AbstractWork {
    private ChannelHandlerContext ctx;
    private Base msg;

    public IRWork(ChannelHandlerContext ctx, Base msg) {
        this.ctx = ctx;
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            AppGameServer gameServer = AppGameServer.getInstance();
            int cmd = msg.getCmd();
            AbsInnerHandler handler = MessagePool.getIns().getInnerHandler(cmd);
            if (handler == null) {
                LogUtil.error("跨服未知协议号, cmd:", cmd);
                return;
            }

            handler.setCtx(ctx);
            handler.setMsg(msg);
            handler.setCmd(cmd);

            if (handler instanceof ClientRoutHandler) { // 直接转发的
                handler.action();
            } else {
                // 所有逻辑进入主线程执行队列
                gameServer.mainLogicServer.addCommand(handler);
            }
        } catch (Exception e) {
            LogUtil.error("跨服协议请求处理错误", e);
        }

    }
}
