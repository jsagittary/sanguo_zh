package com.gryphpoem.game.zw.handler.client.acquisition;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetStatusRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetStatusRs;
import com.gryphpoem.game.zw.service.AcquisitionService;

/**
 * 获取成就
 * 
 * @author tyler
 *
 */
public class GetStatusHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetStatusRq req = msg.getExtension(GetStatusRq.ext);
        AcquisitionService acquisitionService = getService(AcquisitionService.class);
        GetStatusRs resp = acquisitionService.getStatus(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(GetStatusRs.ext, resp);
        }
    }

}
