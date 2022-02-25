package com.gryphpoem.game.zw.handler.client.smallgame;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.pb.GamePb5.GetSmallGameRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetSmallGameRs;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.service.SmallGameService;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-11-24 13:52
 */
public class GetSmallGameHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        msg.getExtension(GamePb5.GetSmallGameRq.ext);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        SmallGameService smallGameService = DataResource.ac.getBean(SmallGameService.class);
        GetSmallGameRs rsp = smallGameService.getSmallGameInfo(player);
        sendMsgToPlayer(GetSmallGameRs.ext, rsp);
    }
}
