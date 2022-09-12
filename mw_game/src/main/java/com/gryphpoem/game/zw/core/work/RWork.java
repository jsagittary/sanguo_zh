package com.gryphpoem.game.zw.core.work;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.AbsClientHandler;
import com.gryphpoem.game.zw.core.handler.AsyncGameHandler;
import com.gryphpoem.game.zw.core.intercept.InterceptAspect;
import com.gryphpoem.game.zw.core.message.MessagePool;
import com.gryphpoem.game.zw.core.net.ConnectServer;
import com.gryphpoem.game.zw.core.util.ChannelUtil;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.handler.client.crosssimple.DirectForwardClientHandler;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb1.BeginGameRq;
import com.gryphpoem.game.zw.pb.GamePb1.CreateRoleRq;
import com.gryphpoem.game.zw.pb.GamePb1.GetNamesRq;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.server.AppGameServer;

import io.netty.channel.ChannelHandlerContext;

public class RWork extends AbstractWork {
    private ChannelHandlerContext ctx;
    private Base msg;

    public RWork(ChannelHandlerContext ctx, Base msg) {
        this.ctx = ctx;
        this.msg = msg;
    }

    @Override
    public void run() {
        AppGameServer gameServer = AppGameServer.getInstance();
        ConnectServer connectServer = gameServer.connectServer;
        try {
            int cmd = msg.getCmd();
            Long roleId = ChannelUtil.getRoleId(ctx);

            // 检查协议前置拦截条件
            if (cmd != BeginGameRq.EXT_FIELD_NUMBER && cmd != CreateRoleRq.EXT_FIELD_NUMBER) {
                MessagePool.getIns().doMessageIntercept(InterceptAspect.CLIENT_MESSAGE, roleId, msg);
            }

            AbsClientHandler handler = MessagePool.getIns().getClientHandler(cmd);
            if (handler == null) {
                LogUtil.error("未知协议号, cmd:", cmd, ", roleId:", roleId);
                return;
            }
            handler.setCtx(ctx);
            handler.setMsg(msg);
            handler.setCmd(cmd);

            if (cmd == BeginGameRq.EXT_FIELD_NUMBER || cmd == GetNamesRq.EXT_FIELD_NUMBER) {
                connectServer.actionExcutor.execute(handler);
            } else if (handler instanceof DirectForwardClientHandler || handler instanceof AsyncGameHandler) {
                // 直接转发的handler 或 执行
                connectServer.actionExcutor.execute(handler);
            } else {
                // 所有玩家逻辑进入主线程执行队列
                gameServer.mainLogicServer.addCommand(handler);
            }
        } catch (MwException me) {// 捕捉游戏自定义异常，并返回相关错误码给客户端
            int rsCmd = MessagePool.getIns().getRsCmd(msg.getCmd());
            if (rsCmd > 0) {
                Base.Builder baseBuilder = PbHelper.createErrorBase(rsCmd, me.getCode(),
                        msg.hasParam() ? msg.getParam() : 0);
                AppGameServer.getInstance().sendMsgToGamer(ctx, baseBuilder);
            }
        } catch (Exception e) {
            LogUtil.error("执行玩家初始化游戏，或添加协议如队列出错", e);
        }

    }
}
