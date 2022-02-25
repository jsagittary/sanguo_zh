package com.gryphpoem.game.zw.handler.client.battlepass;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.BattlePassService;

/**
 * @program: server
 * @description:
 * @author: zhou jie
 * @create: 2019-12-05 20:09
 */
public class ReceiveTaskAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.ReceiveTaskAwardRq req = msg.getExtension(GamePb4.ReceiveTaskAwardRq.ext);

        BattlePassService service = getService(BattlePassService.class);
        GamePb4.ReceiveTaskAwardRs resp = service.receiveTaskAward(getRoleId(), req.getTaskId());

        if (null != resp) {
            sendMsgToPlayer(GamePb4.ReceiveTaskAwardRs.ext, resp);
        }
    }
}