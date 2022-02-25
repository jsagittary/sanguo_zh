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
 * @time: 2021/11/18 16:11
 */
public class TreasureUnlockHeroPosHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.TreasureUnlockHeroPosRq req = msg.getExtension(GamePb4.TreasureUnlockHeroPosRq.ext);
        GamePb4.TreasureUnlockHeroPosRs resp = DataResource.getBean(TreasureCombatService.class).treasureUnlockHeroPos(getRoleId(), req.getIndex());
        if (Objects.nonNull(resp)) {
            sendMsgToPlayer(GamePb4.TreasureUnlockHeroPosRs.ext, resp);
        }
    }
}