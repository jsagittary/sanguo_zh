package com.gryphpoem.game.zw.cmd.server;

import org.springframework.beans.factory.annotation.Autowired;

import com.gryphpoem.game.zw.cmd.base.Cmd;
import com.gryphpoem.game.zw.cmd.base.ServerBaseCommond;
import com.gryphpoem.game.zw.mgr.SessionMgr;
import com.gryphpoem.game.zw.network.util.ChannelUtil;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CrossPb.HeartRq;
import com.gryphpoem.game.zw.pb.CrossPb.HeartRs;

import io.netty.channel.ChannelHandlerContext;

/**
 * @ClassName HeartCmd.java
 * @Description
 * @author QiuKun
 * @date 2019年5月8日
 */
@Cmd(rqCmd = HeartRq.EXT_FIELD_NUMBER, rsCmd = HeartRs.EXT_FIELD_NUMBER)
public class HeartCmd extends ServerBaseCommond {

    @Autowired
    private SessionMgr sessionMgr;

    @Override
    public void execute(ChannelHandlerContext ctx, Base base) throws Exception {
        int serverId = ChannelUtil.getServerIdByCtx(ctx);

        HeartRs.Builder builder = HeartRs.newBuilder();
        builder.setTime(System.currentTimeMillis());

        Base.Builder pbBase = Base.newBuilder();
        pbBase.setCmd(HeartRs.EXT_FIELD_NUMBER);
        pbBase.setExtension(HeartRs.ext, builder.build());

        sessionMgr.sendMsg(pbBase.build(), serverId);
    }

}
