package com.gryphpoem.game.zw.core.handler;

import com.gryphpoem.game.zw.core.util.ChannelUtil;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.server.AppGameServer;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-12 17:45
 */
public abstract class AsyncGameHandler extends AbsClientHandler {
    @Override
    public void sendMsgToPlayer(BasePb.Base.Builder baseBuilder) {
        super.rsMsg = baseBuilder.build();
        AppGameServer.getInstance().sendMsgToGamer(ctx, baseBuilder);
    }

    @Override
    public DealType dealType() {
        return null;
    }

    public Long getRoleId() {
        return ChannelUtil.getRoleId(ctx);
    }
}
