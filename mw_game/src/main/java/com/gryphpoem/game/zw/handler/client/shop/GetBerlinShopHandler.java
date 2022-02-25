package com.gryphpoem.game.zw.handler.client.shop;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetBerlinShopRs;
import com.gryphpoem.game.zw.service.ShopService;

/**
 * @ClassName GetBerlinShopHandler.java
 * @Description 获取柏林银行信息
 * @author QiuKun
 * @date 2018年8月3日
 */
public class GetBerlinShopHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // GetBerlinShopRq req = msg.getExtension(GetBerlinShopRq.ext);
        ShopService service = getService(ShopService.class);
        GetBerlinShopRs resp = service.getBerlinShop(getRoleId());
        if (resp != null) sendMsgToPlayer(GetBerlinShopRs.EXT_FIELD_NUMBER, GetBerlinShopRs.ext, resp);

    }

}
