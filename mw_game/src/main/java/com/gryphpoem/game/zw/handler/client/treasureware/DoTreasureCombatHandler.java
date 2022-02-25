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
 * @time: 2021/11/18 16:09
 */
public class DoTreasureCombatHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.DoTreasureCombatRq req = msg.getExtension(GamePb4.DoTreasureCombatRq.ext);
        GamePb4.DoTreasureCombatRs resp = DataResource.getBean(TreasureCombatService.class).doTreasureCombat(getRoleId(), req);
        if (Objects.nonNull(resp)) {
            sendMsgToPlayer(GamePb4.DoTreasureCombatRs.ext, resp);
        }
    }
}
