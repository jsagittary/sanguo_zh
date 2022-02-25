package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.PlayerService;

/**
 * @program: server
 * @description: 加入社群奖励
 * @author: zhou jie
 * @create: 2019-11-25 11:04
 */
public class JoinCommunityHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        PlayerService playerService = getService(PlayerService.class);
        GamePb4.JoinCommunityRs rs = playerService.joinCommunity(getRoleId());
        if (rs != null) {
            sendMsgToPlayer(GamePb4.JoinCommunityRs.ext, rs);
        }
    }
}