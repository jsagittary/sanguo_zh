package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.HandInChristmasChipRq;
import com.gryphpoem.game.zw.pb.GamePb4.HandInChristmasChipRs;
import com.gryphpoem.game.zw.service.activity.ActivityChristmasService;

public class ChristmasHandInChipHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        HandInChristmasChipRq req = msg.getExtension(HandInChristmasChipRq.ext);
        HandInChristmasChipRs resp = getService(ActivityChristmasService.class).handInChips(getRoleId(),req.getCount(),req.getActivityType());
        if(resp != null){
            sendMsgToPlayer(HandInChristmasChipRs.ext,resp);
        }
    }
}
