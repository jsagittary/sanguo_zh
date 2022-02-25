package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.AutumnTurnplateService;

public class AutumnTurnplateRefreshHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.AutumnTurnplateRefreshRq req = this.msg.getExtension(GamePb4.AutumnTurnplateRefreshRq.ext);
        GamePb4.AutumnTurnplateRefreshRs resp = getService(AutumnTurnplateService.class).refresh(getRoleId(),req.getActType());
        if (resp != null) {
            sendMsgToPlayer(GamePb4.AutumnTurnplateRefreshRs.ext, resp);
        }
    }
}
