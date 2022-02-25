package com.gryphpoem.game.zw.handler.client.newcross;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.cross.serivce.map.Game2CrossAttackService;
import com.gryphpoem.game.zw.pb.GamePb6;

public class ScoutCrossPosHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb6.ScoutCrossPosRq req = msg.getExtension(GamePb6.ScoutCrossPosRq.ext);
        Game2CrossAttackService service = DataResource.ac.getBean(Game2CrossAttackService.class);
        service.scoutCrossPos(getRoleId(), req);
    }
}
