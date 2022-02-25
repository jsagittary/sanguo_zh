package com.gryphpoem.game.zw.handler.client.newcross;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.cross.serivce.map.GameLocal2CrossMapService;
import com.gryphpoem.game.zw.pb.GamePb6;

public class GetNewCrossMarchHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb6.GetCrossWarFireMarchRq req = msg.getExtension(GamePb6.GetCrossWarFireMarchRq.ext);
        GameLocal2CrossMapService service = getService(GameLocal2CrossMapService.class);
        service.getNewCrossMarch(getRoleId(), req.getFunctionId());
    }
}
