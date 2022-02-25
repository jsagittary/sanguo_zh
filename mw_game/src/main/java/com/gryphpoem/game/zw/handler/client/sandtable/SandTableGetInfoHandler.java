package com.gryphpoem.game.zw.handler.client.sandtable;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.sandtable.SandTableContestService;

public class SandTableGetInfoHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.SandTableGetInfoRs resp = getService(SandTableContestService.class).getInfo(getRoleId());
        if(resp != null){
            sendMsgToPlayer(GamePb4.SandTableGetInfoRs.ext,resp);
        }
    }
}
