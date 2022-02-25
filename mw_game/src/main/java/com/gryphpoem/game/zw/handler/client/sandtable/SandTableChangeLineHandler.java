package com.gryphpoem.game.zw.handler.client.sandtable;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.sandtable.SandTableContestService;

public class SandTableChangeLineHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.SandTableChangeLineRq req = this.msg.getExtension(GamePb4.SandTableChangeLineRq.ext);
        GamePb4.SandTableChangeLineRs resp = getService(SandTableContestService.class).changeLine(getRoleId(),req.getLine());
        if(resp != null){
            sendMsgToPlayer(GamePb4.SandTableChangeLineRs.ext,resp);
        }
    }
}
