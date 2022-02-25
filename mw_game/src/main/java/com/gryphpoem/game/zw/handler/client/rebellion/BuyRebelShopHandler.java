package com.gryphpoem.game.zw.handler.client.rebellion;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.BuyRebelShopRq;
import com.gryphpoem.game.zw.pb.GamePb4.BuyRebelShopRs;
import com.gryphpoem.game.zw.service.RebelService;

/**
 * @ClassName BuyRebelShopHandler.java
 * @Description
 * @author QiuKun
 * @date 2018年10月29日
 */
public class BuyRebelShopHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        BuyRebelShopRq req = msg.getExtension(BuyRebelShopRq.ext);
        BuyRebelShopRs resp = getService(RebelService.class).buyRebelShop(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(BuyRebelShopRs.EXT_FIELD_NUMBER, BuyRebelShopRs.ext, resp);
    }

}
