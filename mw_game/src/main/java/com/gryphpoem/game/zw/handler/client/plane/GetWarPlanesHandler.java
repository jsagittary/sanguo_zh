package com.gryphpoem.game.zw.handler.client.plane;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetWarPlanesRs;
import com.gryphpoem.game.zw.service.WarPlaneService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-19 18:01
 * @description: 获取所有战机信息
 * @modified By:
 */
public class GetWarPlanesHandler extends ClientHandler {

    @Override public void action() throws MwException {
        WarPlaneService service = getService(WarPlaneService.class);
        GetWarPlanesRs resp = service.getWarPlanes(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(GetWarPlanesRs.ext, resp);
        }
    }
}
