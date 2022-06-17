package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.activity.ActivityMagicTreasureWareService;

public class DrawMagicTwTurntableAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb5.DrawTwTurntableAwardRq req = msg.getExtension(GamePb5.DrawTwTurntableAwardRq.ext);
        GamePb5.DrawTwTurntableAwardRs resp = getService(ActivityMagicTreasureWareService.class).drawTurntableAward(getRoleId(), req.getId());
        sendMsgToPlayer(GamePb5.DrawTwTurntableAwardRs.EXT_FIELD_NUMBER, GamePb5.DrawTwTurntableAwardRs.ext, resp);
    }
}
