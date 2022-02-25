package com.gryphpoem.game.zw.handler.client.cross;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.CrossWorldMapService;
import com.gryphpoem.game.zw.pb.GamePb5.EnterLeaveCrossRq;
import com.gryphpoem.game.zw.pb.GamePb5.EnterLeaveCrossRs;

/**
 * @author: ZhouJie
 * @date: Create in 2019-04-04 11:33
 * @description:
 * @modified By:
 */
public class EnterLeaveCrossHandler extends ClientHandler {

    @Override public void action() throws MwException {
        EnterLeaveCrossRq req = msg.getExtension(EnterLeaveCrossRq.ext);
        CrossWorldMapService service = getService(CrossWorldMapService.class);
        EnterLeaveCrossRs resp = service.enterLeaveCross(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(EnterLeaveCrossRs.ext, resp);
        }
    }
}
