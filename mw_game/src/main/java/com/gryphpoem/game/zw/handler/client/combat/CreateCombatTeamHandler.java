package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.CreateCombatTeamRq;
import com.gryphpoem.game.zw.pb.GamePb2.CreateCombatTeamRs;
import com.gryphpoem.game.zw.service.MultCombatService;

/**
 * @ClassName CreateCombatTeamHandler.java
 * @Description 创建副本队伍
 * @author QiuKun
 * @date 2018年12月26日
 */
public class CreateCombatTeamHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        CreateCombatTeamRq req = msg.getExtension(CreateCombatTeamRq.ext);
        MultCombatService combatService = getService(MultCombatService.class);
        CreateCombatTeamRs resp = combatService.createCombatTeam(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(CreateCombatTeamRs.EXT_FIELD_NUMBER, CreateCombatTeamRs.ext, resp);
    }

}
