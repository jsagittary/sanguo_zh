package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetChristmasAwardRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetChristmasAwardRs;
import com.gryphpoem.game.zw.service.activity.ActivityChristmasService;

public class ChristmasGetAwardHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GetChristmasAwardRq req = msg.getExtension(GetChristmasAwardRq.ext);
        GetChristmasAwardRs resp = getService(ActivityChristmasService.class).getAward(getRoleId(), req.getStage(),req.getActivityType());
        if (resp != null) {
            sendMsgToPlayer(GetChristmasAwardRs.ext, resp);
        }
    }
}
