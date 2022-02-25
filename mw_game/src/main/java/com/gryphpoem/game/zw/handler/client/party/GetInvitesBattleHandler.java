package com.gryphpoem.game.zw.handler.client.party;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.service.CampService;

/**
 * 获取战斗邀请
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2020-12-08 15:34
 */
public class GetInvitesBattleHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb3.GetInvitesBattleRq req = msg.getExtension(GamePb3.GetInvitesBattleRq.ext);
        CampService campService = getService(CampService.class);
        GamePb3.GetInvitesBattleRs resp = campService.getInvitesBattle(getRoleId(), req.getBattleId(), req.getAirshipId());

        if (null != resp) {
            sendMsgToPlayer(GamePb3.GetInvitesBattleRs.ext, resp);
        }
    }
}