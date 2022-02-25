package com.gryphpoem.game.zw.handler.client.cross.chat;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientAsyncHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb7.GetGamePlayChatRoomRq;
import com.gryphpoem.game.zw.pb.GamePb7.GetGamePlayChatRoomRs;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.service.CrossChatService;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-12-06 16:44
 */
public class GetGamePlayChatRoomHandler extends ClientAsyncHandler {
    @Override
    public void action() throws Exception {
        GetGamePlayChatRoomRq req = msg.getExtension(GetGamePlayChatRoomRq.ext);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        CrossChatService service = DataResource.ac.getBean(CrossChatService.class);
        service.getGamePlayChatRoom(player, req).whenComplete(this::complete);
    }

    @Override
    public <T> void sendMsgToPlayer(T rsp) {
        sendMsgToPlayer(GetGamePlayChatRoomRs.ext, (GetGamePlayChatRoomRs) rsp);
    }
}
