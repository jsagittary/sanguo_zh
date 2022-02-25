package com.gryphpoem.game.zw.handler.client.shop;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.BuyMentorShopRq;
import com.gryphpoem.game.zw.pb.GamePb3.BuyMentorShopRs;
import com.gryphpoem.game.zw.service.ShopService;

/**
 * @ClassName BuyMentorShopHandler.java
 * @Description 荣耀演练场副本商店购买
 * @author QiuKun
 * @date 2018年12月3日
 */
public class BuyMentorShopHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        BuyMentorShopRq req = msg.getExtension(BuyMentorShopRq.ext);
        ShopService service = getService(ShopService.class);
        BuyMentorShopRs resp = service.buyMentorShop(getRoleId(), req);
        sendMsgToPlayer(BuyMentorShopRs.EXT_FIELD_NUMBER, BuyMentorShopRs.ext, resp);
    }

}
