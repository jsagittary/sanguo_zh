package com.gryphpoem.game.zw.handler.client.cross.warfire;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.warfire.WarFireScoreService;
import com.gryphpoem.game.zw.pb.GamePb5.*;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-01-04 17:13
 */
public class GetWarFireCampScoreHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GetWarFireCampScoreRq req = msg.getExtension(GetWarFireCampScoreRq.ext);
        WarFireScoreService service = getService(WarFireScoreService.class);
        GetWarFireCampScoreRs resp = service.getGetWarFireCampScoreDetail(req);
        if (null != resp) {
            sendMsgToPlayer(GetWarFireCampScoreRs.ext, resp);
        }
    }
}
