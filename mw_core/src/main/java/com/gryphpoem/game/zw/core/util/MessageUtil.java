package com.gryphpoem.game.zw.core.util;

import com.gryphpoem.game.zw.pb.BasePb.Base;
import io.netty.channel.ChannelHandlerContext;

public class MessageUtil {
    public static void writeToPlayer(ChannelHandlerContext ctx, Base msg) {
        ctx.channel().writeAndFlush(msg);
    }

    public static byte[] putShort(short s) {
        byte[] b = new byte[2];
        b[0] = (byte) (s >> 8);
        b[1] = (byte) (s >> 0);
        return b;
    }

    static public short getShort(byte[] b, int index) {
        return (short) (((b[index + 1] & 0xff) | b[index + 0] << 8));
    }

    public static int getInt(byte[] bytes) {
        return (bytes[0] << 24) |
                ((bytes[1] & 0xff) << 16) |
                ((bytes[2] & 0xff) << 8) |
                (bytes[3] & 0xff);
    }
}
