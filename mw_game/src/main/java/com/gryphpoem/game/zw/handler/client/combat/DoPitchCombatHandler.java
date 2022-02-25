package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.DoPitchCombatRq;
import com.gryphpoem.game.zw.pb.GamePb2.DoPitchCombatRs;
import com.gryphpoem.game.zw.service.CombatService;

/**
 * @ClassName DoPitchCombatHandler.java
 * @Description 挑战荣耀演习场,与扫荡
 * @author QiuKun
 * @date 2018年12月1日
 */
public class DoPitchCombatHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        DoPitchCombatRq req = msg.getExtension(DoPitchCombatRq.ext);
        CombatService combatService = getService(CombatService.class);
        DoPitchCombatRs resp = combatService.doPitchCombat(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(DoPitchCombatRs.EXT_FIELD_NUMBER, DoPitchCombatRs.ext, resp);
    }

}
