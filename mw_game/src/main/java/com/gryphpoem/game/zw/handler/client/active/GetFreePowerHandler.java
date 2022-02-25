package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetFreePowerRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * 领取体力活动
 * 
 * @author tyler
 *
 */
public class GetFreePowerHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetFreePowerRs resp = getService(ActivityService.class).getFreePower(getRoleId());
        sendMsgToPlayer(GetFreePowerRs.EXT_FIELD_NUMBER, GetFreePowerRs.ext, resp);
    }

}
