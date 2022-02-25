package com.gryphpoem.game.zw.handler.client.common;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.ActionPointRq;
import com.gryphpoem.game.zw.service.PointService;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-01-21 14:31
 * @Description:
 * @Modified By:
 */
public class ActionPointHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        PointService service = getService(PointService.class);
        ActionPointRq req = msg.getExtension(ActionPointRq.ext);
        service.recordPoint(getRoleId(), req);
    }
}
