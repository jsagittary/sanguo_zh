package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.ActivityDragonBoatService;
import com.gryphpoem.game.zw.service.activity.ActivityService;

public class DragonBoatExchangeHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.DragonBoatExchangeRq req = this.msg.getExtension(GamePb4.DragonBoatExchangeRq.ext);
        GamePb4.DragonBoatExchangeRs resp = getService(ActivityDragonBoatService.class).doExchange(getRoleId(),req.getConfigId(),req.getActivityType());
        if (resp != null) {
            sendMsgToPlayer(GamePb4.DragonBoatExchangeRs.ext, resp);
        }
    }
}
