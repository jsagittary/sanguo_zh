package com.gryphpoem.game.zw.handler.client.prop;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.BuyBuildRq;
import com.gryphpoem.game.zw.pb.GamePb1.BuyBuildRs;
import com.gryphpoem.game.zw.service.PropService;

/**
 * 建造队列购买
 * 
 * @author tyler
 *
 */
public class BuyBuildHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        BuyBuildRq req = msg.getExtension(BuyBuildRq.ext);
        BuyBuildRs resp = getService(PropService.class).buyBuild(getRoleId(), req.getType());
        sendMsgToPlayer(BuyBuildRs.EXT_FIELD_NUMBER, BuyBuildRs.ext, resp);
    }

}
