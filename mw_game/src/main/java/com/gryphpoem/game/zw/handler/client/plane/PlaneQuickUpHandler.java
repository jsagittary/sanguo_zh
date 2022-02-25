package com.gryphpoem.game.zw.handler.client.plane;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.WarPlaneService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-19 18:47
 * @description: 战机快速升级
 * @modified By:
 */
public class PlaneQuickUpHandler extends ClientHandler {

    @Override public void action() throws MwException {
        GamePb1.PlaneQuickUpRq req = msg.getExtension(GamePb1.PlaneQuickUpRq.ext);
        WarPlaneService service = getService(WarPlaneService.class);
        GamePb1.PlaneQuickUpRs resp = service.planeQuickUp(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(GamePb1.PlaneQuickUpRs.ext, resp);
        }
    }
}
