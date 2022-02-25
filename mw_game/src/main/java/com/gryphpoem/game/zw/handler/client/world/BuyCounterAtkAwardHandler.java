package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.CounterAtkService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-16 0:52
 * @description: 购买反攻商店的商品
 * @modified By:
 */
public class BuyCounterAtkAwardHandler extends ClientHandler {

    @Override public void action() throws MwException {
        GamePb4.BuyCounterAtkAwardRq req = msg.getExtension(GamePb4.BuyCounterAtkAwardRq.ext);

        CounterAtkService service = getService(CounterAtkService.class);
        GamePb4.BuyCounterAtkAwardRs resp = service.buyCounterAtkAward(req.getId(), getRoleId());

        if (resp != null) {
            sendMsgToPlayer(GamePb4.BuyCounterAtkAwardRs.ext, resp);
        }
    }
}
