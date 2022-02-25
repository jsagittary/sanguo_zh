package com.gryphpoem.game.zw.handler.client.newcross;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.cross.serivce.map.GameLocal2CrossMapService;
import com.gryphpoem.game.zw.pb.GamePb6;

public class NewCrossMoveCityHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb6.CrossWarFireMoveCityRq req = msg.getExtension(GamePb6.CrossWarFireMoveCityRq.ext);
        GameLocal2CrossMapService service = getService(GameLocal2CrossMapService.class);
        service.newCrossMoveCity(getRoleId(), req);
    }
}
