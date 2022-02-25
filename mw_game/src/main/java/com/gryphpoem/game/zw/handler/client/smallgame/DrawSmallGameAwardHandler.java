package com.gryphpoem.game.zw.handler.client.smallgame;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb5.DrawSmallGameAwardRq;
import com.gryphpoem.game.zw.pb.GamePb5.DrawSmallGameAwardRs;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.service.SmallGameService;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-11-24 13:57
 */
public class DrawSmallGameAwardHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        DrawSmallGameAwardRq req = msg.getExtension(DrawSmallGameAwardRq.ext);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        SmallGameService smallGameService = DataResource.ac.getBean(SmallGameService.class);
        DrawSmallGameAwardRs rsp = smallGameService.drawSmallGameAward(player, req);
        sendMsgToPlayer(DrawSmallGameAwardRs.ext, rsp);
    }
}
