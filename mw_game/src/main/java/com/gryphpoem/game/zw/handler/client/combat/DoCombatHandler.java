package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.DoCombatRq;
import com.gryphpoem.game.zw.pb.GamePb2.DoCombatRs;
import com.gryphpoem.game.zw.service.CombatService;

/**
 * 关卡战斗
 * 
 * @author tyler
 *
 */
public class DoCombatHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        DoCombatRq req = msg.getExtension(DoCombatRq.ext);
        CombatService combatService = getService(CombatService.class);
        DoCombatRs resp = combatService.doCombat(getRoleId(), req.getCombatId(), req.getWipe(),req.getHeroIdList());
        if (resp != null) sendMsgToPlayer(DoCombatRs.EXT_FIELD_NUMBER, DoCombatRs.ext, resp);
    }
}
