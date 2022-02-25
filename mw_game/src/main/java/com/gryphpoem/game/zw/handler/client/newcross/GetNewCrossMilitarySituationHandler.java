package com.gryphpoem.game.zw.handler.client.newcross;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.cross.serivce.map.Game2CrossAttackService;
import com.gryphpoem.game.zw.pb.GamePb6;

public class GetNewCrossMilitarySituationHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb6.GetCrossWarFireMilitarySituationRq req = msg.getExtension(GamePb6.GetCrossWarFireMilitarySituationRq.ext);
        DataResource.getBean(Game2CrossAttackService.class).getNewCrossMilitarySituation(getRoleId(), req.getFunctionId());
    }
}
