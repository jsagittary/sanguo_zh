package com.gryphpoem.game.zw.handler.client.cross;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.CrossWorldMapService;
import com.gryphpoem.game.zw.pb.GamePb5.CrossMoveCityRq;
import com.gryphpoem.game.zw.pb.GamePb5.CrossMoveCityRs;

/**
 * @author: ZhouJie
 * @date: Create in 2019-04-04 11:35
 * @description:
 * @modified By:
 */
public class CrossMoveCityHandler extends ClientHandler {

    @Override public void action() throws MwException {
        CrossMoveCityRq req = msg.getExtension(CrossMoveCityRq.ext);
        CrossWorldMapService service = getService(CrossWorldMapService.class);
        CrossMoveCityRs resp = service.crossMoveCity(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(CrossMoveCityRs.ext, resp);
        }
    }
}
