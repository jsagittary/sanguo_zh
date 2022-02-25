package com.gryphpoem.game.zw.handler.client.shop;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.BerlinShopBuyRq;
import com.gryphpoem.game.zw.pb.GamePb3.BerlinShopBuyRs;
import com.gryphpoem.game.zw.service.ShopService;

/**
 * @ClassName BerlinShopBuyHandler.java
 * @Description 柏林银行购买
 * @author QiuKun
 * @date 2018年8月3日
 */
public class BerlinShopBuyHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        BerlinShopBuyRq req = msg.getExtension(BerlinShopBuyRq.ext);
        ShopService service = getService(ShopService.class);
        BerlinShopBuyRs resp = service.berlinShopBuy(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(BerlinShopBuyRs.EXT_FIELD_NUMBER, BerlinShopBuyRs.ext, resp);
    }
}
