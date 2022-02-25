package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.PlayerService;

/**
 * @program: server
 * @description:
 * @author: zhou jie
 * @create: 2019-11-25 19:06
 */
public class GetMixtureDataHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        PlayerService playerService = getService(PlayerService.class);
        GamePb4.GetMixtureDataRs resp = playerService.getMixtureData(getRoleId());
        if (null != resp) {
            sendMsgToPlayer(GamePb4.GetMixtureDataRs.ext, resp);
        }
    }
}