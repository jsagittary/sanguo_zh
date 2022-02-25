package com.gryphpoem.game.zw.handler.client.sandtable;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.sandtable.SandTableContestService;

public class SandTableAdjustLineHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.SandTableAdjustLineRq req = this.msg.getExtension(GamePb4.SandTableAdjustLineRq.ext);
        GamePb4.SandTableAdjustLineRs resp = getService(SandTableContestService.class).adjustLine(getRoleId(),req.getLine1(),req.getLine2());
        if(resp != null){
            sendMsgToPlayer(GamePb4.SandTableAdjustLineRs.ext,resp);
        }
    }
}
