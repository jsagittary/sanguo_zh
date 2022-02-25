package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.TickTeamMemberRq;
import com.gryphpoem.game.zw.pb.GamePb2.TickTeamMemberRs;
import com.gryphpoem.game.zw.service.MultCombatService;

/**
 * @ClassName TickTeamMemberHandler.java
 * @Description 队长踢人
 * @author QiuKun
 * @date 2018年12月26日
 */
public class TickTeamMemberHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
         TickTeamMemberRq req = msg.getExtension(TickTeamMemberRq.ext);
        MultCombatService combatService = getService(MultCombatService.class);
        TickTeamMemberRs resp = combatService.tickTeamMember(getRoleId(),req);
        if (resp != null) sendMsgToPlayer(TickTeamMemberRs.EXT_FIELD_NUMBER, TickTeamMemberRs.ext, resp);
    }

}
