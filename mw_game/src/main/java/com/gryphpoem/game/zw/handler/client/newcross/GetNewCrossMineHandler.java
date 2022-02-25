package com.gryphpoem.game.zw.handler.client.newcross;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.cross.serivce.map.GameLocal2CrossMapService;
import com.gryphpoem.game.zw.pb.GamePb6;

public class GetNewCrossMineHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb6.GetNewCrossMineRq req = msg.getExtension(GamePb6.GetNewCrossMineRq.ext);
        DataResource.getBean(GameLocal2CrossMapService.class).getNewCrossMine(getRoleId(), req);
    }
}
