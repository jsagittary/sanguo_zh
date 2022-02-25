package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.RecentlyBerlinReportRq;
import com.gryphpoem.game.zw.pb.GamePb4.RecentlyBerlinReportRs;
import com.gryphpoem.game.zw.service.BerlinWarService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-07-25 15:54
 * @description: 获取最近战况
 * @modified By:
 */
public class RecentlyBerlinReportHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        RecentlyBerlinReportRq req = msg.getExtension(RecentlyBerlinReportRq.ext);
        BerlinWarService berlinWarService = getService(BerlinWarService.class);
        RecentlyBerlinReportRs resp = berlinWarService.recentlyReport(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(RecentlyBerlinReportRs.ext, resp);
        }
    }
}
