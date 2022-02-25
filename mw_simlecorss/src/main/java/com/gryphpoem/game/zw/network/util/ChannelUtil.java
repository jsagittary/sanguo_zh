package com.gryphpoem.game.zw.network.util;

import com.gryphpoem.game.zw.network.session.SessionGroup;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * @ClassName ChannelUtil.java
 * @Description
 * @author QiuKun
 * @date 2019年5月13日
 */
public abstract class ChannelUtil {

    /**
     * 获取区服id
     * 
     * @param channel
     * @return
     */
    public static int getServerIdByChannel(Channel channel) {
        SessionGroup sessionGroup = channel.attr(AttrKey.SESSION_KEY).get();
        if (sessionGroup != null) {
            return sessionGroup.getTargetServerId();
        }
        return 0;
    }

    public static int getServerIdByCtx(ChannelHandlerContext ctx) {
        return getServerIdByChannel(ctx.channel());
    }
}
