package com.gryphpoem.game.zw.core;

import com.gryphpoem.game.zw.core.net.ConnectServer;
import com.gryphpoem.game.zw.core.net.base.BaseChannelHandler;
import com.gryphpoem.game.zw.core.util.ChannelUtil;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.handler.http.GmHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.server.AppGameServer;

import io.netty.channel.ChannelHandlerContext;

/**
 * 客户端协议到达处理handler
 * 
 * @Description
 * @author TanDonghai
 *
 */
public class ConnectMessageHandler extends BaseChannelHandler {
    private ConnectServer server;

    public ConnectMessageHandler(ConnectServer server) {
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
        super.channelActive(ctx);
        LogUtil.channel("MessageHandler channelActive");
        int total = server.maxConnect.get();
        LogUtil.channel(ctx + " open, total " + total);

        if (total > ConnectServer.MAX_CONNECT) {
            ChannelUtil.closeChannel(ctx, "连接数过多(" + total + ")");
            return;
        } else {
            server.maxConnect.incrementAndGet();
            ChannelUtil.setHeartTime(ctx, System.currentTimeMillis());
        }

        Long index = ChannelUtil.createChannelId(ctx);
        ChannelUtil.setChannelId(ctx, index);
        ChannelUtil.setRoleId(ctx, 0L);

        AppGameServer.getInstance().userChannels.put(index, ctx);
        LogUtil.channel("session max create:" + server.maxConnect.get());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        LogUtil.channel("MessageHandler channelInactive");
        int total = server.maxConnect.decrementAndGet();
        LogUtil.channel(ctx + " close, total " + total);
        long roleId = ChannelUtil.getRoleId(ctx);

        AppGameServer.getInstance().userChannels.remove(ChannelUtil.createChannelId(ctx));
        AppGameServer.getInstance().gamerExit(ctx, roleId);
        LogUtil.common("玩家退出登录, roleId::" + roleId);
        
        //玩家离开游戏，发送玩家的角色信息到账号服
        PlayerDataManager playerDataManager = (PlayerDataManager) AppGameServer.ac.getBean("playerDataManager");
		Player player = playerDataManager.getPlayer(roleId);
		GmHandler.sendRoleToAccount(player);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtil.channel("MessageHandler exceptionCaught!" + cause.getMessage());
        ctx.close();
    }
}
