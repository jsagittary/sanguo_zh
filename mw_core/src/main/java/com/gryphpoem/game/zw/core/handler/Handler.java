package com.gryphpoem.game.zw.core.handler;

import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.gryphpoem.game.zw.core.ICommand;
import com.gryphpoem.game.zw.pb.BasePb.Base;

import io.netty.channel.ChannelHandlerContext;

public abstract class Handler implements ICommand {
    public static final int PUBLIC = 0;
    public static final int MAIN = 1;
    public static final int BUILD_QUE = 2;
    public static final int TANK_QUE = 3;

    protected Base rsMsg;
    protected int rsMsgCmd;
    protected ChannelHandlerContext ctx;
    protected Base msg;
    protected long createTime;

    protected int cmd;

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public Handler(ChannelHandlerContext ctx, Base msg) {
        this.ctx = ctx;
        this.msg = msg;
        setCreateTime(System.currentTimeMillis());
    }

    public Handler() {
        setCreateTime(System.currentTimeMillis());
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

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public <T> Base.Builder createRsBase(int cmd, int code, GeneratedExtension<Base, T> ext, T msg) {
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(cmd);
        baseBuilder.setCode(code);
        if (this.msg.hasParam()) {
            baseBuilder.setParam(this.msg.getParam());
        }
        if (msg != null && ext != null) {
            baseBuilder.setExtension(ext, msg);
        }
        return baseBuilder;
    }

    public <T> Base.Builder createRsBase(int code, GeneratedExtension<Base, T> ext, T msg) {
        return this.createRsBase(rsMsgCmd, code, ext, msg);
    }

    public Base.Builder createRsBase(int code) {
        return this.createRsBase(rsMsgCmd, code, null, null);
    }

    public abstract DealType dealType();

    public int getRsMsgCmd() {
        return rsMsgCmd;
    }

    public void setRsMsgCmd(int rsMsgCmd) {
        this.rsMsgCmd = rsMsgCmd;
    }

    public Base getRsMsg() {
        return rsMsg;
    }
}
