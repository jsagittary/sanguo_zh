package com.gryphpoem.game.zw.cmd.base;

import com.gryphpoem.game.zw.pb.BasePb.Base;

import io.netty.channel.ChannelHandlerContext;

/**
 * @ClassName ServerBaseCommond.java
 * @Description 游戏服 --> 跨服 [非玩家操作] 协议基类
 * @author QiuKun
 * @date 2019年4月30日
 */
public abstract class ServerBaseCommond extends Command {

    @Override
    public void execute(ChannelHandlerContext ctx, Base base, Object obj) throws Exception {
        execute(ctx, base);
    }

    public abstract void execute(ChannelHandlerContext ctx, Base base) throws Exception;
}
