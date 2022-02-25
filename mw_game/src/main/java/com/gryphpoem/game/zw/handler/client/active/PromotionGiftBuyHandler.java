package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.PromotionPropBuyRq;
import com.gryphpoem.game.zw.pb.GamePb3.PromotionPropBuyRs;
import com.gryphpoem.game.zw.service.ShopService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-05-25 14:25
 * @description: 打折礼包购买
 * @modified By:
 */
public class PromotionGiftBuyHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        PromotionPropBuyRq req = msg.getExtension(PromotionPropBuyRq.ext);
        PromotionPropBuyRs res = getService(ShopService.class).promotionGiftBuy(req, getRoleId());
        sendMsgToPlayer(PromotionPropBuyRs.EXT_FIELD_NUMBER, PromotionPropBuyRs.ext, res);
    }
}
