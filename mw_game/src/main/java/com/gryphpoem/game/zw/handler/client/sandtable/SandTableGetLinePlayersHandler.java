package com.gryphpoem.game.zw.handler.client.sandtable;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.sandtable.SandTableContestService;

public class SandTableGetLinePlayersHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.SandTableGetLinePlayersRq req = this.msg.getExtension(GamePb4.SandTableGetLinePlayersRq.ext);
        GamePb4.SandTableGetLinePlayersRs resp = getService(SandTableContestService.class).getLinePlayers(this.getRoleId(),req.getLine());
        if(resp != null){
            sendMsgToPlayer(GamePb4.SandTableGetLinePlayersRs.ext,resp);
        }
    }
}
