package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.SupplyDorpBuyRq;
import com.gryphpoem.game.zw.pb.GamePb3.SupplyDorpBuyRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @ClassName SupplyDorpBuyHandler.java
 * @Description 空降补给购买
 * @author QiuKun
 * @date 2017年7月25日
 */
public class SupplyDorpBuyHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        SupplyDorpBuyRq req = msg.getExtension(SupplyDorpBuyRq.ext);
        ActivityService service = getService(ActivityService.class);
        SupplyDorpBuyRs res = service.supplyDorpBuy(getRoleId(), req.getParam());
        if (res != null) sendMsgToPlayer(SupplyDorpBuyRs.ext, res);

    }

}
