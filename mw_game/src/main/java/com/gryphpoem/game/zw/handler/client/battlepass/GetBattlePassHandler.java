package com.gryphpoem.game.zw.handler.client.battlepass;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.BattlePassService;

/**
 * @program: server
 * @description:
 * @author: zhou jie
 * @create: 2019-12-05 20:08
 */
public class GetBattlePassHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        BattlePassService service = getService(BattlePassService.class);
        GamePb4.GetBattlePassRs resp = service.getBattlePass(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(GamePb4.GetBattlePassRs.ext, resp);
        }
    }
}