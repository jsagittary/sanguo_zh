package com.gryphpoem.game.zw.handler.client.plane;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.PlaneFactoryRs;
import com.gryphpoem.game.zw.service.WarPlaneService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-19 18:38
 * @description: 空军基地的信息
 * @modified By:
 */
public class PlaneFactoryHandler extends ClientHandler {

    @Override public void action() throws MwException {
        WarPlaneService service = getService(WarPlaneService.class);
        PlaneFactoryRs resp = service.planeFactory(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(PlaneFactoryRs.ext, resp);
        }
    }
}
