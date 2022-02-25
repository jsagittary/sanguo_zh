package com.gryphpoem.game.zw.core.net.proxy;

import com.gryphpoem.game.zw.core.net.base.BaseChannelHandler;
import com.gryphpoem.game.zw.pb.BasePb.Base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class InnerChannelInboundHandler extends SimpleChannelInboundHandler<Base> {

	private static InnerChannelInboundHandler ins = new InnerChannelInboundHandler();

	public static InnerChannelInboundHandler getIns() {
		return ins;
	}

	private InnerChannelInboundHandler() {

	}

	private BaseChannelHandler agent;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Base msg) throws Exception {
		agent.channelRead0(ctx, msg);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		agent.channelRegistered(ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		agent.channelUnregistered(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		agent.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		agent.channelInactive(ctx);
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		agent.exceptionCaught(ctx, cause);
	}

	public BaseChannelHandler getAgent() {
		return agent;
	}

	public void setAgent(BaseChannelHandler agent) {
		this.agent = agent;
	}

}
