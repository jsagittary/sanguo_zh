package com.gryphpoem.game.zw.handler.client.plane;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.SyntheticPlaneRq;
import com.gryphpoem.game.zw.pb.GamePb1.SyntheticPlaneRs;
import com.gryphpoem.game.zw.service.WarPlaneService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-19 18:43
 * @description: 合成战机
 * @modified By:
 */
public class SyntheticPlaneHandler extends ClientHandler {

    @Override public void action() throws MwException {
        SyntheticPlaneRq req = msg.getExtension(SyntheticPlaneRq.ext);
        WarPlaneService service = getService(WarPlaneService.class);
        SyntheticPlaneRs resp = service.syntheticPlane(getRoleId(), req.getPlaneType());

        if (null != resp) {
            sendMsgToPlayer(SyntheticPlaneRs.ext, resp);
        }
    }
}
