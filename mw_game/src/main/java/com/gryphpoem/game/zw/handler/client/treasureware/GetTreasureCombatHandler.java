package com.gryphpoem.game.zw.handler.client.treasureware;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.TreasureCombatService;

import java.util.Objects;

/**
 * @description:
 * @author: zhou jie
 * @time: 2021/11/18 16:05
 */
public class GetTreasureCombatHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.GetTreasureCombatRs resp = DataResource.getBean(TreasureCombatService.class).getTreasureCombat(getRoleId());
        if (Objects.nonNull(resp)) {
            sendMsgToPlayer(GamePb4.GetTreasureCombatRs.ext, resp);
        }
    }
}
