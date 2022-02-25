package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.activity.Year2022FishService;

public class YearFishShopExchangeHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb5.YearFishShopExchangeRq req = this.msg.getExtension(GamePb5.YearFishShopExchangeRq.ext);
        GamePb5.YearFishShopExchangeRs resp = getService(Year2022FishService.class).shopExchange(getRoleId(),req.getActType(),req.getConfId());
        if (resp != null) {
            sendMsgToPlayer(GamePb5.YearFishShopExchangeRs.ext, resp);
        }
    }
}
