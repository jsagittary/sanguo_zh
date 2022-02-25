package com.gryphpoem.game.zw.handler.client.army;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.FactoryService;

/**
 * @program: empire_activity
 * @description:
 * @author: zhou jie
 * @create: 2020-05-27 16:15
 */
public class QuickBuyArmyHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        FactoryService service = getService(FactoryService.class);
        GamePb1.QuickBuyArmyRs resp = service.quickBuyArmy(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(GamePb1.QuickBuyArmyRs.ext, resp);
        }
    }
}