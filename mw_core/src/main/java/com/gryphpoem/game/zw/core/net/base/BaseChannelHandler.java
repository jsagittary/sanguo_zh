package com.gryphpoem.game.zw.core.net.base;

import com.gryphpoem.game.zw.pb.BasePb.Base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BaseChannelHandler extends SimpleChannelInboundHandler<Base> {

	public void channelRead0(ChannelHandlerContext ctx, Base msg) throws Exception {
	}

}
