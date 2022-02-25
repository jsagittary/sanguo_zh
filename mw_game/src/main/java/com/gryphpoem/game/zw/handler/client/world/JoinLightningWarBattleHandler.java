package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.JoinLightningWarBattleRq;
import com.gryphpoem.game.zw.pb.GamePb4.JoinLightningWarBattleRs;
import com.gryphpoem.game.zw.service.LightningWarService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-05-17 14:44
 * @description: 加入闪电战
 * @modified By:
 */
public class JoinLightningWarBattleHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        JoinLightningWarBattleRq req = msg.getExtension(JoinLightningWarBattleRq.ext);
        LightningWarService service = getService(LightningWarService.class);
        JoinLightningWarBattleRs resp = service.joinLightningWarBattle(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(JoinLightningWarBattleRs.ext, resp);
        }
    }
}
