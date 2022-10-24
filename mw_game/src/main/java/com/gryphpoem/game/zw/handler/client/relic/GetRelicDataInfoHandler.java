package com.gryphpoem.game.zw.handler.client.relic;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb6;
import com.gryphpoem.game.zw.service.relic.RelicService;

public class GetRelicDataInfoHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb6.GetRelicDataInfoRq rq = msg.getExtension(GamePb6.GetRelicDataInfoRq.ext);
        GamePb6.GetRelicDataInfoRs req = getService(RelicService.class).getRelicDataInfo(getRoleId());
        sendMsgToPlayer(GamePb6.GetRelicDataInfoRs.ext, req);
    }
}
