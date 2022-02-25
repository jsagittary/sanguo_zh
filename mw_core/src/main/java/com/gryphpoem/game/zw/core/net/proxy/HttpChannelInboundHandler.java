package com.gryphpoem.game.zw.core.net.proxy;

import com.gryphpoem.game.zw.core.net.base.HttpBaseChannelHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class HttpChannelInboundHandler extends ChannelInboundHandlerAdapter {
	private static HttpChannelInboundHandler ins = new HttpChannelInboundHandler();

	public static HttpChannelInboundHandler getIns() {
		return ins;
	}

	private HttpChannelInboundHandler() {
	}

	private HttpBaseChannelHandler agent;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		agent.channelRead(ctx, msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		agent.channelReadComplete(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		agent.exceptionCaught(ctx, cause);
	}

	public HttpBaseChannelHandler getAgent() {
		return agent;
	}

	public void setAgent(HttpBaseChannelHandler agent) {
		this.agent = agent;
	}
}
