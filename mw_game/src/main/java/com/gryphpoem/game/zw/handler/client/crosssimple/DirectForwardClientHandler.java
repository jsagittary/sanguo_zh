package com.gryphpoem.game.zw.handler.client.crosssimple;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.server.AppGameServer;

/**
 * @ClassName DirectForwardClientHandler.java
 * @Description 直接转发客户端请求到跨服
 * @author QiuKun
 * @date 2019年5月16日
 */
public class DirectForwardClientHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        Base.Builder baseBuilder = Base.newBuilder(msg);
        AppGameServer.getInstance().sendMsgToCross(baseBuilder, getRoleId().longValue());
    }

}
