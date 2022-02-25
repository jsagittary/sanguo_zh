package com.gryphpoem.game.zw.handler.client.plane;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.SearchPlaneRs;
import com.gryphpoem.game.zw.pb.GamePb1.SearchPlaneRq;
import com.gryphpoem.game.zw.service.WarPlaneService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-19 18:40
 * @description: 战机寻访
 * @modified By:
 */
public class SearchPlaneHandler extends ClientHandler {

    @Override public void action() throws MwException {
        SearchPlaneRq req = msg.getExtension(SearchPlaneRq.ext);
        WarPlaneService service = getService(WarPlaneService.class);
        SearchPlaneRs resp = service.searchPlane(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(SearchPlaneRs.ext, resp);
        }
    }
}
