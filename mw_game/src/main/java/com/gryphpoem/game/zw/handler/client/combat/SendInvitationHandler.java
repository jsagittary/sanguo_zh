package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.SendInvitationRq;
import com.gryphpoem.game.zw.pb.GamePb2.SendInvitationRs;
import com.gryphpoem.game.zw.service.MultCombatService;

/**
 * @ClassName SendInvitationHandler.java
 * @Description 发送邀请
 * @author QiuKun
 * @date 2018年12月26日
 */
public class SendInvitationHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        SendInvitationRq req = msg.getExtension(SendInvitationRq.ext);
        MultCombatService combatService = getService(MultCombatService.class);
        SendInvitationRs resp = combatService.sendInvitation(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(SendInvitationRs.EXT_FIELD_NUMBER, SendInvitationRs.ext, resp);
    }

}
