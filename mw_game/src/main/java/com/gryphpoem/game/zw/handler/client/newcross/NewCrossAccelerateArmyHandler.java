package com.gryphpoem.game.zw.handler.client.newcross;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.cross.serivce.map.Game2CrossArmyService;
import com.gryphpoem.game.zw.pb.GamePb6;

public class NewCrossAccelerateArmyHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb6.NewCrossAccelerateArmyRq req = msg.getExtension(GamePb6.NewCrossAccelerateArmyRq.ext);
        DataResource.getBean(Game2CrossArmyService.class).accelerateCrossArmy(getRoleId(), req);

//        if (Objects.nonNull(resp)) {
//            sendMsgToPlayer(GamePb6.NewCrossAccelerateArmyRs.ext, resp);
//        }
    }
}
