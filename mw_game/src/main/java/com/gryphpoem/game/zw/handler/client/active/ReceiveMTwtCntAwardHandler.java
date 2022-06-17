package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.activity.ActivityMagicTreasureWareService;

public class ReceiveMTwtCntAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb5.ReceiveMtwTurntableCntAwardRq req = msg.getExtension(GamePb5.ReceiveMtwTurntableCntAwardRq.ext);
        GamePb5.ReceiveMtwTurntableCntAwardRs resp = getService(ActivityMagicTreasureWareService.class).
                receiveMtwTurntableCntAward(getRoleId(), req);
        sendMsgToPlayer(GamePb5.ReceiveMtwTurntableCntAwardRs.EXT_FIELD_NUMBER, GamePb5.ReceiveMtwTurntableCntAwardRs.ext, resp);
    }
}
