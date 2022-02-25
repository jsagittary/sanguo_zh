package com.gryphpoem.game.zw.handler.client.party;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.pb.GamePb3.SupplyRewardRs;
import com.gryphpoem.game.zw.pb.GamePb3.SupplyRewardRq;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.service.CampService;

/**
 * @author: ZhouJie
 * @date: Create in 2019-02-21 10:40
 * @description: 领取补给奖励
 * @modified By:
 */
public class SupplyRewardHandler extends ClientHandler {

    @Override public void action() throws MwException {
        SupplyRewardRq req = msg.getExtension(SupplyRewardRq.ext);
        CampService service = getService(CampService.class);
        GamePb3.SupplyRewardRs resp = service.supplyReward(getRoleId(), req);

        if (!CheckNull.isNull(resp)) {
            sendMsgToPlayer(SupplyRewardRs.ext, resp);
        }
    }
}
