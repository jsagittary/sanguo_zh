package com.gryphpoem.game.zw.handler.client.treasureware;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.TreasureWareService;
import java.util.Objects;

public class MakeTreasureWareHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.MakeTreasureWareRq req = msg.getExtension(GamePb4.MakeTreasureWareRq.ext);
        GamePb4.MakeTreasureWareRs resp = DataResource.getBean(TreasureWareService.class).makeTreasureWare(getRoleId(), req.getQuality(), req.getCount());
        if (Objects.nonNull(resp)) {
            sendMsgToPlayer(GamePb4.MakeTreasureWareRs.ext, resp);
        }
    }
}
