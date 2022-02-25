package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.AutumnTurnplateService;

public class AutumnTurnplatePlayHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.AutumnTurnplatePlayRq req = this.msg.getExtension(GamePb4.AutumnTurnplatePlayRq.ext);
        GamePb4.AutumnTurnplatePlayRs resp = getService(AutumnTurnplateService.class).play(getRoleId(),req.getActType(),req.getPlayCount());
        if (resp != null) {
            sendMsgToPlayer(GamePb4.AutumnTurnplatePlayRs.ext, resp);
        }
    }
}
