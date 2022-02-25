package com.gryphpoem.game.zw.network.tcp;

import java.util.concurrent.atomic.AtomicInteger;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BasePb.Base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @ClassName SocketChannelHandler.java
 * @Description
 * @author QiuKun
 * @date 2019年4月29日
 */
public abstract class SocketChannelHandler extends SimpleChannelInboundHandler<Base> {
    private String handlerName;
    private AtomicInteger connectNum;

    public SocketChannelHandler(String handlerName) {
        this.handlerName = handlerName;
        this.connectNum = new AtomicInteger(0);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtil.error(handlerName + " exceptionCaughted , ctx:" + ctx + cause);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        LogUtil.channel(handlerName + " opened, total " + connectNum.incrementAndGet() + " ctx:" + ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        LogUtil.channel(handlerName + " closed, total " + connectNum.decrementAndGet() + " ctx:" + ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                LogUtil.channel("HeartbeatHandler trigger READER_IDLE");
                ctx.close();
            }
        }
    }
}
