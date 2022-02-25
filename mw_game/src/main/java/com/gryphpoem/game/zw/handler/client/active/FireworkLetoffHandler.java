package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.activity.Year2022FireworkService;

public class FireworkLetoffHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb5.FireworkLetoffRq req = this.msg.getExtension(GamePb5.FireworkLetoffRq.ext);
        GamePb5.FireworkLetoffRs resp = getService(Year2022FireworkService.class).letOff(getRoleId(),req.getConfId(),req.getActType());
        if (resp != null) {
            sendMsgToPlayer(GamePb5.FireworkLetoffRs.ext, resp);
        }
    }
}
