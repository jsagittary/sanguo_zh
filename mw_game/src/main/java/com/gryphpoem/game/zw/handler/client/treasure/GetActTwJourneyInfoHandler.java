package com.gryphpoem.game.zw.handler.client.treasure;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.activity.task.TreasureWareTaskActService;

public class GetActTwJourneyInfoHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb5.GetActTwJourneyRq req = msg.getExtension(GamePb5.GetActTwJourneyRq.ext);
        GamePb5.GetActTwJourneyRs resp = getService(TreasureWareTaskActService.class).getActTwJourneyInfo(getRoleId(), req);
        sendMsgToPlayer(GamePb5.GetActTwJourneyRs.EXT_FIELD_NUMBER, GamePb5.GetActTwJourneyRs.ext, resp);
    }
}
