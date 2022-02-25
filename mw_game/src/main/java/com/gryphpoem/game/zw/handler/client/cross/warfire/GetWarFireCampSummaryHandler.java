package com.gryphpoem.game.zw.handler.client.cross.warfire;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.warfire.WarFireScoreService;
import com.gryphpoem.game.zw.pb.GamePb5.*;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-01-05 11:47
 */
public class GetWarFireCampSummaryHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GetWarFireCampSummaryRq req = msg.getExtension(GetWarFireCampSummaryRq.ext);
        WarFireScoreService service = getService(WarFireScoreService.class);
        GetWarFireCampSummaryRs resp = service.getCampSummary(req);
        if (null != resp) {
            sendMsgToPlayer(GetWarFireCampSummaryRs.ext, resp);
        }
    }
}
