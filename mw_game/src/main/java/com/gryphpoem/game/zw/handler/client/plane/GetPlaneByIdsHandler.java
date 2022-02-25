package com.gryphpoem.game.zw.handler.client.plane;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetPlaneByIdsRq;
import com.gryphpoem.game.zw.pb.GamePb1.GetPlaneByIdsRs;
import com.gryphpoem.game.zw.service.WarPlaneService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-19 18:04
 * @description: 获取部分战机数据
 * @modified By:
 */
public class GetPlaneByIdsHandler extends ClientHandler {

    @Override public void action() throws MwException {
        GetPlaneByIdsRq req = msg.getExtension(GetPlaneByIdsRq.ext);
        WarPlaneService warPlaneService = getService(WarPlaneService.class);
        GetPlaneByIdsRs resp = warPlaneService.getPlaneByIds(getRoleId(), req.getPlaneIdsList());

        if (null != resp) {
            sendMsgToPlayer(GetPlaneByIdsRs.ext, resp);
        }
    }
}
