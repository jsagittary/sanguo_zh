package com.gryphpoem.game.zw.handler.client.party;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.SupplyHallRs;
import com.gryphpoem.game.zw.service.CampService;

/**
 * @author: ZhouJie
 * @date: Create in 2019-02-21 10:38
 * @description: 补给大厅的信息
 * @modified By:
 */
public class SupplyHallHandler extends ClientHandler {

    @Override public void action() throws MwException {
        CampService service = getService(CampService.class);
        SupplyHallRs resp = service.supplyHall(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(SupplyHallRs.ext, resp);
        }
    }
}
