package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.AutumnTurnplateService;

public class AutumnTurnplateGetProgressAwardHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.AutumnTurnplateGetProgressAwardRq req = this.msg.getExtension(GamePb4.AutumnTurnplateGetProgressAwardRq.ext);
        GamePb4.AutumnTurnplateGetProgressAwardRs resp = getService(AutumnTurnplateService.class).getProgressAward(getRoleId(),req.getActType(),req.getConfId());
        if (resp != null) {
            sendMsgToPlayer(GamePb4.AutumnTurnplateGetProgressAwardRs.ext, resp);
        }
    }
}
