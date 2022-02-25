package com.gryphpoem.game.zw.handler.client.plane;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.PlaneSwapRq;
import com.gryphpoem.game.zw.pb.GamePb1.PlaneSwapRs;
import com.gryphpoem.game.zw.service.WarPlaneService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-19 18:10
 * @description: 战机替换
 * @modified By:
 */
public class PlaneSwapHandler extends ClientHandler {

    @Override public void action() throws MwException {
        PlaneSwapRq req = msg.getExtension(PlaneSwapRq.ext);
        WarPlaneService service = getService(WarPlaneService.class);
        PlaneSwapRs resp = service.planeSwap(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(PlaneSwapRs.ext, resp);
        }
    }
}
