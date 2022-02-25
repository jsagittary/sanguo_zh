package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.LeaveCombatTeamRs;
import com.gryphpoem.game.zw.service.MultCombatService;

/**
 * @ClassName LeaveCombatTeamHandler.java
 * @Description 离开队伍
 * @author QiuKun
 * @date 2018年12月26日
 */
public class LeaveCombatTeamHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // LeaveCombatTeamRq req = msg.getExtension(LeaveCombatTeamRq.ext);
        MultCombatService combatService = getService(MultCombatService.class);
        LeaveCombatTeamRs resp = combatService.leaveCombatTeam(getRoleId());
        if (resp != null) sendMsgToPlayer(LeaveCombatTeamRs.EXT_FIELD_NUMBER, LeaveCombatTeamRs.ext, resp);
    }

}
