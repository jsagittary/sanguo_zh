package com.gryphpoem.game.zw.resource.domain;

import com.gryphpoem.game.zw.pb.BasePb.Base;

import io.netty.channel.ChannelHandlerContext;

public class Msg {
	private ChannelHandlerContext ctx;
	private Base msg;
	private long roleId;

	public Msg(ChannelHandlerContext ctx, Base msg, long roleId) {
		this.ctx = ctx;
		this.msg = msg;
		this.roleId = roleId;
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	public Base getMsg() {
		return msg;
	}

	public void setMsg(Base msg) {
		this.msg = msg;
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}
}
