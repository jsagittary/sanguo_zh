package com.gryphpoem.game.zw.cmd.base;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.pb.BasePb.Base;

import io.netty.channel.ChannelHandlerContext;

/**
 * @ClassName PlayerBaseCommond.java
 * @Description 游戏服 --> 跨服 [玩家操作] 协议基类
 * @author QiuKun
 * @date 2019年5月5日
 */
public abstract class PlayerBaseCommond extends Command {

    @Override
    public void execute(ChannelHandlerContext ctx, Base base, Object obj) throws Exception {
        if (obj == null) {
            LogUtil.error("CrossPlayer be null,lordId=" + base.getLordId() + " ,code=" + base.getCmd());
            return;
        }
        if (obj instanceof CrossPlayer) {
            CrossPlayer p = (CrossPlayer) obj;
            p.focus(); // 设置为在线
            execute(p, base);
        }
    }

    public abstract void execute(CrossPlayer player, Base base) throws Exception;
}
