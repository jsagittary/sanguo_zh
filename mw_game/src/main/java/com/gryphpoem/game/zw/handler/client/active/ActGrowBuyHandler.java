package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.ActGrowBuyRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * 购买成长计划
 * @author tyler
 *
 */
public class ActGrowBuyHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ActGrowBuyRs resp = getService(ActivityService.class).growBuy(getRoleId());
        sendMsgToPlayer(ActGrowBuyRs.EXT_FIELD_NUMBER, ActGrowBuyRs.ext, resp);
    }

}
