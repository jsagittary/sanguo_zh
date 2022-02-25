package com.gryphpoem.game.zw.handler.client.cross.warfire;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.warfire.WarFireCommonService;
import com.gryphpoem.game.zw.pb.GamePb5.BuyWarFireShopRq;
import com.gryphpoem.game.zw.pb.GamePb5.BuyWarFireShopRs;

/**
 * 战火燎原兑换
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2021-01-08 17:16
 */
public class BuyWarFireShopHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        BuyWarFireShopRq req = msg.getExtension(BuyWarFireShopRq.ext);
        WarFireCommonService service = getService(WarFireCommonService.class);
        BuyWarFireShopRs resp = service.buyWarFireShop(getRoleId(), req.getShopId());
        if (null != resp) {
            sendMsgToPlayer(BuyWarFireShopRs.ext, resp);
        }
    }
}