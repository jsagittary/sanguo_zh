package com.gryphpoem.game.zw.handler.client.battlepass;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.BattlePassService;

/**
 * @program: server
 * @description:
 * @author: zhou jie
 * @create: 2019-12-05 20:11
 */
public class BuyBattlePassLvHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.BuyBattlePassLvRq req = msg.getExtension(GamePb4.BuyBattlePassLvRq.ext);

        BattlePassService service = getService(BattlePassService.class);
        GamePb4.BuyBattlePassLvRs resp = service.buyBattlePassLv(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(GamePb4.BuyBattlePassLvRs.ext, resp);
        }
    }
}