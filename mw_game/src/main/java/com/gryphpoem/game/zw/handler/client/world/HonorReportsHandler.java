package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetHonorReportsRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetHonorReportsRs;
import com.gryphpoem.game.zw.service.HonorDailyService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-08-10 3:26
 * @description:
 * @modified By:
 */
public class HonorReportsHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetHonorReportsRq req = msg.getExtension(GetHonorReportsRq.ext);
        HonorDailyService honorDailyService = getService(HonorDailyService.class);
        GetHonorReportsRs resp = honorDailyService.getHonorReports(getRoleId(), req.getType());

        if (null != resp) {
            sendMsgToPlayer(GetHonorReportsRs.ext, resp);
        }
    }
}
