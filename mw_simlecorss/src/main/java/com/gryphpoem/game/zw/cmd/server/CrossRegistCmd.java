package com.gryphpoem.game.zw.cmd.server;

import org.springframework.beans.factory.annotation.Autowired;

import com.gryphpoem.game.zw.cmd.base.Cmd;
import com.gryphpoem.game.zw.cmd.base.ServerBaseCommond;
import com.gryphpoem.game.zw.mgr.SessionMgr;
import com.gryphpoem.game.zw.network.session.SessionGroup;
import com.gryphpoem.game.zw.network.session.SocketSession;
import com.gryphpoem.game.zw.network.util.AttrKey;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CrossPb.ServerRegistRq;

import io.netty.channel.ChannelHandlerContext;

/**
 * @ClassName CrossRegistRqCmd.java
 * @Description 游戏向账号服注册服务的协议
 * @author QiuKun
 * @date 2019年5月5日
 */
@Cmd(rqCmd = ServerRegistRq.EXT_FIELD_NUMBER)
public class CrossRegistCmd extends ServerBaseCommond {
    
    @Autowired
    private SessionMgr sessionMgr;
    
    @Override
    public void execute(ChannelHandlerContext ctx, Base base) throws Exception {
        ServerRegistRq req = base.getExtension(ServerRegistRq.ext);
        int serverId = req.getServerId();
        int total = req.getTotal();
        int index = req.getIndex();

        SocketSession session = new SocketSession(index, ctx.channel());
        
        SessionGroup group = sessionMgr.addSession(session, serverId, total, index);

        ctx.channel().attr(AttrKey.SESSION_KEY).set(group);
        ctx.channel().attr(AttrKey.SESSION_INDEX).set(index);
    }

}
