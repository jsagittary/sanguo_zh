package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.activity.Year2022LongLightService;

public class LongLightIgniteHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb5.LongLightIgniteRq req = this.msg.getExtension(GamePb5.LongLightIgniteRq.ext);
        GamePb5.LongLightIgniteRs resp = getService(Year2022LongLightService.class).ignite(getRoleId(),req.getActType());
        if (resp != null) {
            sendMsgToPlayer(GamePb5.LongLightIgniteRs.ext, resp);
        }
    }
}
