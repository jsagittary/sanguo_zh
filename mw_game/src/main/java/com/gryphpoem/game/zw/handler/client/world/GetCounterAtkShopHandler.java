package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.CounterAtkService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-16 0:51
 * @description: 获取反攻商店信息
 * @modified By:
 */
public class GetCounterAtkShopHandler extends ClientHandler {

    @Override public void action() throws MwException {
        CounterAtkService service = getService(CounterAtkService.class);
        GamePb4.GetCounterAtkShopRs resp = service.getCounterAtkShop(getRoleId());

        if (resp != null) {
            sendMsgToPlayer(GamePb4.GetCounterAtkShopRs.ext, resp);
        }
    }
}
