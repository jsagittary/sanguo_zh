package com.gryphpoem.game.zw.handler.client.prop;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.DecisiveBattleRs;
import com.gryphpoem.game.zw.service.DecisiveBattleService;

/**
 * 决战道具产出
 *
 * @author ZhuJianJian
 */
public class DecisiveBattleHandler extends ClientHandler {

    @Override public void action() throws MwException {
        DecisiveBattleRs resp = getService(DecisiveBattleService.class).getDecisiveBattleInstruction(getRoleId());
        if (null != resp) {
            sendMsgToPlayer(DecisiveBattleRs.ext, resp);
        }
    }
}
