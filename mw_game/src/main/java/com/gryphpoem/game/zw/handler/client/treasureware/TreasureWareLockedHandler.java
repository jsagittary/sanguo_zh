package com.gryphpoem.game.zw.handler.client.treasureware;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.TreasureWareService;

import java.util.Objects;

public class TreasureWareLockedHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.TreasureWareLockedRs resp = DataResource.getBean(TreasureWareService.class).
                treasureWareLocked(getRoleId(), msg.getExtension(GamePb4.TreasureWareLockedRq.ext));
        if (Objects.nonNull(resp)) {
            sendMsgToPlayer(GamePb4.TreasureWareLockedRs.ext, resp);
        }
    }
}
