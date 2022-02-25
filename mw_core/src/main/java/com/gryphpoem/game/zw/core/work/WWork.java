package com.gryphpoem.game.zw.core.work;

import com.gryphpoem.game.zw.core.util.ChannelUtil;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import io.netty.channel.ChannelHandlerContext;

public class WWork extends AbstractWork {
	private ChannelHandlerContext ctx;
	private Base msg;

	public WWork(ChannelHandlerContext ctx, Base msg) {
		this.ctx = ctx;
		this.msg = msg;
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public Base getMsg() {
		return msg;
	}

	@Override
    public void run() {
		try {
			LogUtil.c2sMessage(msg, ChannelUtil.getRoleId(ctx));
			ctx.writeAndFlush(msg);
		} catch (Exception e) {
			LogUtil.error("向客服端写入协议数据出错", e);
		}
	}
}
