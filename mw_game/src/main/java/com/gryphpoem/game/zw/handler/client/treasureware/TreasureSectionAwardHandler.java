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
 * @time: 2021/11/22 13:50
 */
public class TreasureSectionAwardHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.TreasureSectionAwardRq req = msg.getExtension(GamePb4.TreasureSectionAwardRq.ext);
        GamePb4.TreasureSectionAwardRs resp = DataResource.getBean(TreasureCombatService.class).treasureSectionAward(getRoleId(), req.getCombatId());
        if (Objects.nonNull(resp)) {
            sendMsgToPlayer(GamePb4.TreasureSectionAwardRs.ext, resp);
        }
    }
}
