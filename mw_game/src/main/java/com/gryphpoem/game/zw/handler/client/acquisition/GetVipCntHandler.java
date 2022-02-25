package com.gryphpoem.game.zw.handler.client.acquisition;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetVipCntRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetVipCntRs;
import com.gryphpoem.game.zw.service.AcquisitionService;

public class GetVipCntHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetVipCntRq req = msg.getExtension(GetVipCntRq.ext);
        AcquisitionService acquisitionService = getService(AcquisitionService.class);
        GetVipCntRs resp = acquisitionService.getVipCnt(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(GetVipCntRs.ext, resp);
        }
    }

}
