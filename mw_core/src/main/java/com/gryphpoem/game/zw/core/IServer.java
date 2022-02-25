package com.gryphpoem.game.zw.core;

import com.gryphpoem.game.zw.pb.BasePb.Base;

import io.netty.channel.ChannelHandlerContext;

public interface IServer {

	/**
	 * 处理消息
	 * 
	 * @param ctx
	 * @param msg
	 */
	public abstract void doCommand(ChannelHandlerContext ctx, Base msg);

	/**
	 * 打开
	 * 
	 * @param ctx
	 */
	public void channelActive(ChannelHandlerContext ctx);

	/**
	 * 关闭
	 * 
	 * @param ctx
	 */
	public void channelInactive(ChannelHandlerContext ctx);
}
