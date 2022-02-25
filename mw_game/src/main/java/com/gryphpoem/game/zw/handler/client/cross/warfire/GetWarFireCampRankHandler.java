package com.gryphpoem.game.zw.handler.client.cross.warfire;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.warfire.WarFireScoreService;
import com.gryphpoem.game.zw.pb.GamePb5.*;

/**
 * 战火燎原阵营排名
 *
 * @Description
 * @Author zhangdh
 * @Date 2021-01-04 17:10
 */
public class GetWarFireCampRankHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetWarFireCampRankRq req = msg.getExtension(GetWarFireCampRankRq.ext);
        WarFireScoreService service = getService(WarFireScoreService.class);
        GetWarFireCampRankRs resp = service.getWarFireCampRankRq(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(GetWarFireCampRankRs.ext, resp);
        }
    }
}
