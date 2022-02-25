package com.gryphpoem.game.zw.core;

import com.gryphpoem.game.zw.core.common.ChannelAttr;
import com.gryphpoem.game.zw.core.net.InnerServer;
import com.gryphpoem.game.zw.core.net.base.BaseChannelHandler;
import com.gryphpoem.game.zw.core.util.ChannelUtil;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BasePb.Base;

import io.netty.channel.ChannelHandlerContext;

/**
 * 内部消息通讯处理handler，跨服使用
 * 
 * @Description
 * @author TanDonghai
 *
 */
public class InnerMessageHandler extends BaseChannelHandler {
    private InnerServer server;

    public InnerMessageHandler(InnerServer server) {
        this.server = server;
    }

    public void channelRead0(ChannelHandlerContext ctx, Base msg) throws Exception {
        server.channelRead(ctx, msg);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        LogUtil.channel("MessageHandler channelRegistered");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        LogUtil.channel("MessageHandler channelUnregistered");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Long channelId = ChannelUtil.createChannelId(ctx);
        // 注意是放绑定到channel中,而非ctx中
        ctx.channel().attr(ChannelAttr.ID).set(channelId);
        LogUtil.channel("MessageHandler 注册channelId成功 channelId:" + channelId);
        super.channelActive(ctx);
        LogUtil.channel("MessageHandler channelActive");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.channel("MessageHandler channelInactive");
        Integer index = ctx.channel().attr(ChannelAttr.SESSION_INDEX).get();
        if (index != null) {
            server.retryConnect(index);
        }
        super.channelInactive(ctx);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtil.error("InnerMessageHandler exceptionCaught!", cause);
        ctx.close();
    }
}
