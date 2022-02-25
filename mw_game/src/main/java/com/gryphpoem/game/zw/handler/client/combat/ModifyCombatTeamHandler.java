package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.ModifyCombatTeamRq;
import com.gryphpoem.game.zw.pb.GamePb2.ModifyCombatTeamRs;
import com.gryphpoem.game.zw.service.MultCombatService;

/**
 * @ClassName ModifyCombatTeamHandler.java
 * @Description 修改副本队伍信息(解散队伍,修改状态)
 * @author QiuKun
 * @date 2018年12月26日
 */
public class ModifyCombatTeamHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ModifyCombatTeamRq req = msg.getExtension(ModifyCombatTeamRq.ext);
        MultCombatService combatService = getService(MultCombatService.class);
        ModifyCombatTeamRs resp = combatService.modifyCombatTeam(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(ModifyCombatTeamRs.EXT_FIELD_NUMBER, ModifyCombatTeamRs.ext, resp);
    }

}
