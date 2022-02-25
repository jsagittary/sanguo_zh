package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.AttackDecisiveBattleRq;
import com.gryphpoem.game.zw.pb.GamePb4.AttackDecisiveBattleRs;
import com.gryphpoem.game.zw.service.DecisiveBattleService;

/**
 * 决战某个坐标的势力
 */
public class AttackDecisiveBattleHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        AttackDecisiveBattleRq req = msg.getExtension(AttackDecisiveBattleRq.ext);
        DecisiveBattleService battleService = getService(DecisiveBattleService.class);
        AttackDecisiveBattleRs res = battleService.decisiveBattleRs(getRoleId(), req);
        if (null != res) {
            sendMsgToPlayer(AttackDecisiveBattleRs.ext, res);
        }
    }

}
