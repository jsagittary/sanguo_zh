package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetPitchCombatRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetPitchCombatRs;
import com.gryphpoem.game.zw.service.CombatService;

/**
 * @ClassName GetPitchCombatHandler.java
 * @Description 获取荣耀演习场副本
 * @author QiuKun
 * @date 2018年12月1日
 */
public class GetPitchCombatHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetPitchCombatRq req = msg.getExtension(GetPitchCombatRq.ext);
        CombatService combatService = getService(CombatService.class);
        GetPitchCombatRs resp = combatService.getPitchCombat(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(GetPitchCombatRs.EXT_FIELD_NUMBER, GetPitchCombatRs.ext, resp);
    }

}
