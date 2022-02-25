package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.SummerTurntableService;

public class SummerTurntablePlayHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.SummerTurntablePlayRq req = this.msg.getExtension(GamePb4.SummerTurntablePlayRq.ext);
        GamePb4.SummerTurntablePlayRs resp = getService(SummerTurntableService.class).playTurntable(getRoleId(),req.getCount(),req.getActType());
        if (resp != null) {
            sendMsgToPlayer(GamePb4.SummerTurntablePlayRs.ext, resp);
        }
    }
}
