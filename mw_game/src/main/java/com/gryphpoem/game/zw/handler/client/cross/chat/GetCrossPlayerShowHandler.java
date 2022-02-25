package com.gryphpoem.game.zw.handler.client.cross.chat;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientAsyncHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb7.GetCrossPlayerShowRq;
import com.gryphpoem.game.zw.pb.GamePb7.GetCrossPlayerShowRs;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.service.CrossChatService;


/**
 * @Description
 * @Author zhangdh
 * @Date 2021-12-29 22:59
 */
public class GetCrossPlayerShowHandler extends ClientAsyncHandler {
    @Override
    public void action() throws Exception {
        GetCrossPlayerShowRq req = msg.getExtension(GetCrossPlayerShowRq.ext);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        CrossChatService service = DataResource.ac.getBean(CrossChatService.class);
        service.getCrossPlayerShow(player, req).whenComplete(super::complete);
    }

    @Override
    public <T> void sendMsgToPlayer(T rsp) {
        sendMsgToPlayer(GetCrossPlayerShowRs.ext, (GetCrossPlayerShowRs)rsp);
    }
}
