package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.JoinGestapoBattleRq;
import com.gryphpoem.game.zw.pb.GamePb4.JoinGestapoBattleRs;
import com.gryphpoem.game.zw.service.GestapoService;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-03-29 16:18
 * @Description: 加入盖世太保的战斗
 * @Modified By:
 */
public class JoinGestapoBattleHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        JoinGestapoBattleRq req = msg.getExtension(JoinGestapoBattleRq.ext);
        GestapoService service = getService(GestapoService.class);
        JoinGestapoBattleRs resp = service.joinGestapoBattle(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(JoinGestapoBattleRs.ext, resp);
        }
    }
}
