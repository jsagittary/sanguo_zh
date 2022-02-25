package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.CounterAtkService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-16 0:48
 * @description: 进攻反攻boss
 * @modified By:
 */
public class AttackCounterBossHandler extends ClientHandler {

    @Override public void action() throws MwException {
        GamePb4.AttackCounterBossRq req = msg.getExtension(GamePb4.AttackCounterBossRq.ext);

        CounterAtkService service = getService(CounterAtkService.class);
        GamePb4.AttackCounterBossRs resp = service.attackCounterBoss(req, getRoleId());

        if (resp != null) {
            sendMsgToPlayer(GamePb4.AttackCounterBossRs.ext, resp);
        }
    }
}
