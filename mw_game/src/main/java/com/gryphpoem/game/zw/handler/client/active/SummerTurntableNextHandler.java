package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.SummerTurntableService;

public class SummerTurntableNextHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.SummerTurntableNextRq req = this.msg.getExtension(GamePb4.SummerTurntableNextRq.ext);
        GamePb4.SummerTurntableNextRs resp = getService(SummerTurntableService.class).nextRound(getRoleId(),req.getActType());
        if (resp != null) {
            sendMsgToPlayer(GamePb4.SummerTurntableNextRs.ext, resp);
        }
    }
}
