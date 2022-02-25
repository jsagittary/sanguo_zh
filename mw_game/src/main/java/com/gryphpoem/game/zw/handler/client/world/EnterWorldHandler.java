package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.EnterWorldRq;
import com.gryphpoem.game.zw.pb.GamePb2.EnterWorldRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * @ClassName EnterWorldHandler.java
 * @Description 进入世界/或返回基地
 * @author QiuKun
 * @date 2017年9月14日
 */
public class EnterWorldHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        EnterWorldRq req = msg.getExtension(EnterWorldRq.ext);
        WorldService service = getService(WorldService.class);
        EnterWorldRs resp = service.enterWorld(getRoleId(), req.getIsEnter());
        if (null != resp) {
            sendMsgToPlayer(EnterWorldRs.ext, resp);
        }
    }

}
