package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.AnniversaryEggService;

public class AnniversaryEggOpenHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.AnniversaryEggOpenRq req = this.msg.getExtension(GamePb4.AnniversaryEggOpenRq.ext);
        GamePb4.AnniversaryEggOpenRs resp = getService(AnniversaryEggService.class).openEgg(getRoleId(),req.getActType(),req.getEggId());
        if (resp != null) {
            sendMsgToPlayer(GamePb4.AnniversaryEggOpenRs.ext, resp);
        }
    }
}
