package com.gryphpoem.game.zw.cmd.base;

import com.gryphpoem.game.zw.pb.BasePb.Base;

import io.netty.channel.ChannelHandlerContext;

/**
 * @ClassName Command.java
 * @Description
 * @author QiuKun
 * @date 2019年4月29日
 */
public abstract class Command {
    // 请求协议
    private int rqCmd;
    // 返回协议
    private int rsCmd;

    public void init(int rqCmd, int rsCmd) {
        this.rqCmd = rqCmd;
        this.rsCmd = rsCmd;
    }

    public int getRqCmd() {
        return rqCmd;
    }

    public int getRsCmd() {
        return rsCmd;
    }

    public abstract void execute(ChannelHandlerContext ctx, Base base, Object obj) throws Exception;
}
