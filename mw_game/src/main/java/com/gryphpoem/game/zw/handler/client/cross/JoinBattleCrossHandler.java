package com.gryphpoem.game.zw.handler.client.cross;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.CrossWorldMapService;
import com.gryphpoem.game.zw.pb.GamePb5.JoinBattleCrossRq;
import com.gryphpoem.game.zw.pb.GamePb5.JoinBattleCrossRs;

/**
 * @author: ZhouJie
 * @date: Create in 2019-04-04 11:38
 * @description:
 * @modified By:
 */
public class JoinBattleCrossHandler extends ClientHandler {

    @Override public void action() throws MwException {
        JoinBattleCrossRq req = msg.getExtension(JoinBattleCrossRq.ext);
        CrossWorldMapService service = getService(CrossWorldMapService.class);
        JoinBattleCrossRs resp = service.joinBattleCross(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(JoinBattleCrossRs.ext, resp);
        }
    }
}
