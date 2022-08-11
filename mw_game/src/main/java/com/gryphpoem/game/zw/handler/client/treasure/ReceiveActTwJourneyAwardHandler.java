package com.gryphpoem.game.zw.handler.client.treasure;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.activity.task.TreasureWareTaskActService;

public class ReceiveActTwJourneyAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb5.ReceiveActTwJourneyAwardRq req = msg.getExtension(GamePb5.ReceiveActTwJourneyAwardRq.ext);
        GamePb5.ReceiveActTwJourneyAwardRs resp = getService(TreasureWareTaskActService.class).receiveActTwJourneyAward(getRoleId(), req);
        sendMsgToPlayer(GamePb5.ReceiveActTwJourneyAwardRs.EXT_FIELD_NUMBER, GamePb5.ReceiveActTwJourneyAwardRs.ext, resp);
    }
}
