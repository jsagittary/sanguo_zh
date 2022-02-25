package com.gryphpoem.game.zw.handler.client.plane;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.PlaneRemouldRs;
import com.gryphpoem.game.zw.pb.GamePb1.PlaneRemouldRq;
import com.gryphpoem.game.zw.service.WarPlaneService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-19 18:36
 * @description: 战机改造
 * @modified By:
 */
public class PlaneRemouldHandler extends ClientHandler {

    @Override public void action() throws MwException {
        PlaneRemouldRq req = msg.getExtension(PlaneRemouldRq.ext);
        WarPlaneService service = getService(WarPlaneService.class);
        PlaneRemouldRs resp = service.planeRemould(getRoleId(), req.getPlaneId());

        if (null != resp) {
            sendMsgToPlayer(PlaneRemouldRs.ext, resp);
        }
    }
}
