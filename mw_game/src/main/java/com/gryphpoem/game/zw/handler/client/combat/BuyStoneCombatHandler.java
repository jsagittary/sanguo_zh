package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.BuyStoneCombatRs;
import com.gryphpoem.game.zw.service.CombatService;

/**
 * @ClassName BuyStoneCombatHandler.java
 * @Description
 * @author QiuKun
 * @date 2018年5月12日
 */
public class BuyStoneCombatHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // BuyStoneCombatRq req = msg.getExtension(BuyStoneCombatRq.ext);
        CombatService combatService = getService(CombatService.class);
        BuyStoneCombatRs resp = combatService.buyStoneCombat(getRoleId());
        sendMsgToPlayer(BuyStoneCombatRs.EXT_FIELD_NUMBER, BuyStoneCombatRs.ext, resp);
    }

}
