package com.gryphpoem.game.zw.core.work;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BasePb.Base;

import io.netty.channel.Channel;

/**
 * @ClassName SendMsgWork.java
 * @Description
 * @author QiuKun
 * @date 2019年5月9日
 */
public class SendMsgWork extends AbstractWork {

    private Channel channel;
    private Base msg;

    public SendMsgWork(Channel channel, Base msg) {
        this.channel = channel;
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            channel.writeAndFlush(msg);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

}
