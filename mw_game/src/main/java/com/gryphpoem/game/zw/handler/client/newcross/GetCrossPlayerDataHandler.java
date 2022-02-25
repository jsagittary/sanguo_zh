package com.gryphpoem.game.zw.handler.client.newcross;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.cross.serivce.CrossGamePlayService;
import com.gryphpoem.game.zw.pb.GamePb6;

public class GetCrossPlayerDataHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb6.GetCrossPlayerDataRq req = msg.getExtension(GamePb6.GetCrossPlayerDataRq.ext);
        CrossGamePlayService service = getService(CrossGamePlayService.class);
        GamePb6.GetCrossPlayerDataRs resp = service.getCrossPlayerData(getRoleId(), req.getFunctionId());
        if (null != resp) {
            sendMsgToPlayer(GamePb6.GetCrossPlayerDataRs.ext, resp);
        }

    }
}
