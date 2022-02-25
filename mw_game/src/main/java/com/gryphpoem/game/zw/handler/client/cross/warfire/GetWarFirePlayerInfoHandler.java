package com.gryphpoem.game.zw.handler.client.cross.warfire;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.warfire.WarFireScoreService;
import com.gryphpoem.game.zw.pb.GamePb5.*;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-01-07 21:24
 */
public class GetWarFirePlayerInfoHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GetPlayerWarFireRq req = msg.getExtension(GetPlayerWarFireRq.ext);
        WarFireScoreService service = getService(WarFireScoreService.class);
        GetPlayerWarFireRs resp = service.getPlayerWarFireInfo(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(GetPlayerWarFireRs.ext, resp);
        }
    }
}
