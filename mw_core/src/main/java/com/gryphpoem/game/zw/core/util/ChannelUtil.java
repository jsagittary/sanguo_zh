package com.gryphpoem.game.zw.core.util;

import java.net.InetSocketAddress;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.gryphpoem.game.zw.core.common.ChannelAttr;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;

public class ChannelUtil {
    private static Logger logger = LogManager.getLogger(ChannelUtil.class);

    public static void closeChannel(ChannelHandlerContext ctx, String reason) {
        logger.error(ctx + "-->close [because] " + reason);
        ctx.close();
    }

    public static Long getChannelId(ChannelHandlerContext ctx) {
        return ctx.attr(ChannelAttr.ID).get();
    }

    public static void setChannelId(ChannelHandlerContext ctx, Long id) {
        Attribute<Long> attribute = ctx.attr(ChannelAttr.ID);
        attribute.set(id);
    }

    public static Long createChannelId(ChannelHandlerContext ctx) {
        return createChannelId(ctx.channel());
    }

    public static Long createChannelId(Channel channel) {
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        String ip = address.getAddress().getHostAddress();
        int port = address.getPort();
        Long id = ip2long(ip) * 100000L + port;
        return id;
    }

    public static String getIp(ChannelHandlerContext ctx) {
        if (null == ctx) {
            return null;
        }

        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        return address.getAddress().getHostAddress();
    }

    public static void setRoleId(ChannelHandlerContext ctx, Long roleId) {
        Attribute<Long> attribute = ctx.attr(ChannelAttr.roleId);
        attribute.set(roleId);
    }

    public static void setHeartTime(ChannelHandlerContext ctx, Long nowTime) {
        Attribute<Long> attribute = ctx.attr(ChannelAttr.heartTime);
        attribute.set(nowTime);
    }

    public static Long getRoleId(ChannelHandlerContext ctx) {
        if (ctx == null) {
            return null;
        }
        Attribute<Long> attribute = ctx.attr(ChannelAttr.roleId);
        if (attribute == null) {
            logger.error(ctx + "-->attribute is null [because] " + ChannelAttr.roleId);
            return 0L;
        }
        return attribute.get();
    }

    public static Long getHeartTime(ChannelHandlerContext ctx) {
        return ctx.attr(ChannelAttr.heartTime).get();
    }

    /**
     * IP转成整型
     * 
     * @param ip
     * @return
     */
    private static Long ip2long(String ip) {
        Long num = 0L;
        if (ip == null) {
            return num;
        }

        try {
            ip = ip.replaceAll("[^0-9\\.]", ""); // 去除字符串前的空字符
            String[] ips = ip.split("\\.");
            if (ips.length == 4) {
                num = Long.parseLong(ips[0], 10) * 256L * 256L * 256L + Long.parseLong(ips[1], 10) * 256L * 256L
                        + Long.parseLong(ips[2], 10) * 256L + Long.parseLong(ips[3], 10);
                num = num >>> 0;
            }
        } catch (NullPointerException ex) {
            System.out.println(ip);
        }

        return num;
    }
}
