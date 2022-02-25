package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetTeamMemberListRs;
import com.gryphpoem.game.zw.service.MultCombatService;

/**
 * @ClassName GetTeamMemberListHandler.java
 * @Description 获取可选队员列表
 * @author QiuKun
 * @date 2018年12月26日
 */
public class GetTeamMemberListHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // GetTeamMemberListRq req = msg.getExtension(GetTeamMemberListRq.ext);
        MultCombatService combatService = getService(MultCombatService.class);
        GetTeamMemberListRs resp = combatService.getTeamMemberList(getRoleId());
        if (resp != null) sendMsgToPlayer(GetTeamMemberListRs.EXT_FIELD_NUMBER, GetTeamMemberListRs.ext, resp);
    }

}
