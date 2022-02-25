package com.gryphpoem.game.zw.handler.client.treasureware;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.TreasureWareService;

import java.util.Objects;

public class OnTreasureWareHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.OnTreasureWareRq req = msg.getExtension(GamePb4.OnTreasureWareRq.ext);
        GamePb4.OnTreasureWareRs resp = DataResource.getBean(TreasureWareService.class).onTreasureWare(getRoleId(), req);
        if (Objects.nonNull(resp)) {
            sendMsgToPlayer(GamePb4.OnTreasureWareRs.ext, resp);
        }
    }
}
