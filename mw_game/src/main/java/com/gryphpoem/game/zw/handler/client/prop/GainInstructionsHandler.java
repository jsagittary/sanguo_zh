package com.gryphpoem.game.zw.handler.client.prop;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GainInstructionsRs;
import com.gryphpoem.game.zw.service.DecisiveBattleService;

public class GainInstructionsHandler extends ClientHandler {

    @Override public void action() throws MwException {
        GainInstructionsRs res = getService(DecisiveBattleService.class).getGainInstructions(getRoleId());
        sendMsgToPlayer(GainInstructionsRs.ext, res);
    }

}
