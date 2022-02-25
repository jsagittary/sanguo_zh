package com.gryphpoem.game.zw.handler.client.newcross;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.cross.serivce.map.Game2CrossAttackService;
import com.gryphpoem.game.zw.pb.GamePb6;

public class GetNewCrossBattleHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb6.GetCrossWarFireBattleRq req = msg.getExtension(GamePb6.GetCrossWarFireBattleRq.ext);
        Game2CrossAttackService service = getService(Game2CrossAttackService.class);
        service.getNewCrossBattle(getRoleId(), req);
    }
}
