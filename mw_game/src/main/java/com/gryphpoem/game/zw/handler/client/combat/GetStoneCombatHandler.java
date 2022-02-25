package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetStoneCombatRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetStoneCombatRs;
import com.gryphpoem.game.zw.service.CombatService;

/**
 * @ClassName GetStoneCombatHandler.java
 * @Description
 * @author QiuKun
 * @date 2018年5月12日
 */
public class GetStoneCombatHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetStoneCombatRq req = msg.getExtension(GetStoneCombatRq.ext);
        CombatService combatService = getService(CombatService.class);
        GetStoneCombatRs resp = combatService.getStoneCombat(getRoleId(), req);
        sendMsgToPlayer(GetStoneCombatRs.EXT_FIELD_NUMBER, GetStoneCombatRs.ext, resp);
    }

}
