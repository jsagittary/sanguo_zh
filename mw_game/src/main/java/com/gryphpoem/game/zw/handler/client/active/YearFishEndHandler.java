package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.activity.Year2022FishService;

public class YearFishEndHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb5.YearFishEndRq req = this.msg.getExtension(GamePb5.YearFishEndRq.ext);
        GamePb5.YearFishEndRs resp = getService(Year2022FishService.class).endFishing(getRoleId(),req.getActType(),req.getFishKeysList());
        if (resp != null) {
            sendMsgToPlayer(GamePb5.YearFishEndRs.ext, resp);
        }
    }
}
