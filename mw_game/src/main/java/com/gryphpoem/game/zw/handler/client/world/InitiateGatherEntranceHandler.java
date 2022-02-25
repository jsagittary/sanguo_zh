package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * @ClassName InitiateGatherEntranceHandler.java
 * @Description 发起集结入口
 */
public class InitiateGatherEntranceHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        WorldService service = getService(WorldService.class);
        GamePb2.InitiateGatherEntranceRs resp = service.initiateGatherEntrance(getRoleId());
        if (resp != null)
        {
            sendMsgToPlayer(GamePb2.InitiateGatherEntranceRs.ext, resp);
        }
    }
}
