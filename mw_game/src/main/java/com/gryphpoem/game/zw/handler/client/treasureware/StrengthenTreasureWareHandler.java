package com.gryphpoem.game.zw.handler.client.treasureware;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.TreasureWareService;

import java.util.Objects;

public class StrengthenTreasureWareHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.StrengthenTreasureWareRq req = msg.getExtension(GamePb4.StrengthenTreasureWareRq.ext);
        GamePb4.StrengthenTreasureWareRs resp = DataResource.getBean(TreasureWareService.class).
                strengthenTreasureWare(getRoleId(), req.getKeyId());
        if (Objects.nonNull(resp)) {
            sendMsgToPlayer(GamePb4.StrengthenTreasureWareRs.ext, resp);
        }
    }
}
