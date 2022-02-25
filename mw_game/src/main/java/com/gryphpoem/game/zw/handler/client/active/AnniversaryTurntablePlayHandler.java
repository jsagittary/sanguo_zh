package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.AnniversaryTurntableService;

public class AnniversaryTurntablePlayHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.AnniversaryTurntablePlayRq req = this.msg.getExtension(GamePb4.AnniversaryTurntablePlayRq.ext);
        GamePb4.AnniversaryTurntablePlayRs resp = getService(AnniversaryTurntableService.class).playTurntable(getRoleId(),req.getActType(),req.getTimes());
        if (resp != null) {
            sendMsgToPlayer(GamePb4.AnniversaryTurntablePlayRs.ext, resp);
        }
    }
}
