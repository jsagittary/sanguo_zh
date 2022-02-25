package com.gryphpoem.game.zw.cmd.server;

import org.springframework.beans.factory.annotation.Autowired;

import com.gryphpoem.game.zw.cmd.base.Cmd;
import com.gryphpoem.game.zw.cmd.base.ServerBaseCommond;
import com.gryphpoem.game.zw.network.util.ChannelUtil;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CrossPb.CrossLoginRq;
import com.gryphpoem.game.zw.service.CrossPlayerService;

import io.netty.channel.ChannelHandlerContext;

/**
 * @ClassName CrossLoginCmd.java
 * @Description 玩家登陆或退出跨服
 * @author QiuKun
 * @date 2019年5月13日
 */
@Cmd(rqCmd = CrossLoginRq.EXT_FIELD_NUMBER)
public class CrossLoginCmd extends ServerBaseCommond {

    @Autowired
    private CrossPlayerService crossPlayerService;

    @Override
    public void execute(ChannelHandlerContext ctx, Base base) throws Exception {
        long lordId = base.getLordId();
        CrossLoginRq req = base.getExtension(CrossLoginRq.ext);
        int serverId = ChannelUtil.getServerIdByCtx(ctx);
        crossPlayerService.loginPlayerDispatch(serverId, lordId, req);
    }

}
