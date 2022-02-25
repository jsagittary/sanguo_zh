package com.gryphpoem.game.zw.network.session;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BasePb.Base;

import io.netty.channel.Channel;

public class SocketSession {

    private int index;
    private Channel channel;

    public SocketSession(int index, Channel channel) {
        this.index = index;
        this.channel = channel;
    }

    public void write(Base msg) {
        channel.writeAndFlush(msg);
        LogUtil.innerMessage(msg);
    }

    public boolean isAlive() {
        try {
            if (channel == null) {
                return false;
            }
            return channel.isActive();
        } catch (Exception e) {
            LogUtil.error("连接状态异常", e);
            return false;
        }
    }

    public void close() {
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (Exception e) {
            LogUtil.error("连接关闭异常", e);
        }
    }

    public int getIndex() {
        return index;
    }

    public Channel getChannel() {
        return channel;
    }

}
