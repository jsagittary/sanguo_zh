package com.gryphpoem.game.zw.handler.client.party;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.service.CampService;

/**
 * 发起邀请
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2020-12-08 15:36
 */
public class MakeInvitesHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb3.MakeInvitesRq req = msg.getExtension(GamePb3.MakeInvitesRq.ext);
        CampService campService = getService(CampService.class);
        GamePb3.MakeInvitesRs resp = campService.makeInvites(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(GamePb3.MakeInvitesRs.ext, resp);
        }
    }
}