package com.gryphpoem.game.zw.handler.client.sandtable;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.sandtable.SandTableContestService;

public class SandTableUpdateArmyHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.SandTableUpdateArmyRq req = this.msg.getExtension(GamePb4.SandTableUpdateArmyRq.ext);
        GamePb4.SandTableUpdateArmyRs resp = getService(SandTableContestService.class).updateArmy(getRoleId(),req.getHeroIdList());
        if(resp != null){
            sendMsgToPlayer(GamePb4.SandTableUpdateArmyRs.ext,resp);
        }
    }
}
