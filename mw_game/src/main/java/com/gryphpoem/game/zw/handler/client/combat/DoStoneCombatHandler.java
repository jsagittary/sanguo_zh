package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.DoStoneCombatRq;
import com.gryphpoem.game.zw.pb.GamePb2.DoStoneCombatRs;
import com.gryphpoem.game.zw.service.CombatService;

/**
 * @ClassName DoStoneCombatHandler.java
 * @Description
 * @author QiuKun
 * @date 2018年5月12日
 */
public class DoStoneCombatHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        DoStoneCombatRq req = msg.getExtension(DoStoneCombatRq.ext);
        CombatService combatService = getService(CombatService.class);
        DoStoneCombatRs resp = combatService.doStoneCombat(getRoleId(), req.getCombatId(), req.getWipe(),
                req.getUseProp(), req.getHeroIdList());
        sendMsgToPlayer(DoStoneCombatRs.EXT_FIELD_NUMBER, DoStoneCombatRs.ext, resp);
    }

}
