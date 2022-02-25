package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.TriggerGiftBuyRq;
import com.gryphpoem.game.zw.pb.GamePb3.TriggerGiftBuyRs;
import com.gryphpoem.game.zw.service.ShopService;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-03-03 17:26
 * @Description: 金币购买触发式礼包
 * @Modified By:
 */
public class TriggerGIftBuyHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        TriggerGiftBuyRq req = msg.getExtension(TriggerGiftBuyRq.ext);
        TriggerGiftBuyRs res = getService(ShopService.class).triggerGiftBuy(req, getRoleId());
        sendMsgToPlayer(TriggerGiftBuyRs.EXT_FIELD_NUMBER, TriggerGiftBuyRs.ext, res);
    }
}
