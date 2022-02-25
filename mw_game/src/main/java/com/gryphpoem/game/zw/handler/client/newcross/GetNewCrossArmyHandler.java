package com.gryphpoem.game.zw.handler.client.newcross;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.cross.serivce.map.Game2CrossArmyService;
import com.gryphpoem.game.zw.pb.GamePb6;

public class GetNewCrossArmyHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb6.GetCrossWarFireArmyRq req = msg.getExtension(GamePb6.GetCrossWarFireArmyRq.ext);
        Game2CrossArmyService service = getService(Game2CrossArmyService.class);
        service.getNewCrossArmy(getRoleId(), req.getFunctionId());
    }
}
