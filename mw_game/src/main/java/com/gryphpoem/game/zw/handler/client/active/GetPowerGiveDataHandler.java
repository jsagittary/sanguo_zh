package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetPowerGiveDataRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

public class GetPowerGiveDataHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetPowerGiveDataRs resp = getService(ActivityService.class).getPowerGiveData(getRoleId());
        sendMsgToPlayer(GetPowerGiveDataRs.EXT_FIELD_NUMBER, GetPowerGiveDataRs.ext, resp);
    }

}
