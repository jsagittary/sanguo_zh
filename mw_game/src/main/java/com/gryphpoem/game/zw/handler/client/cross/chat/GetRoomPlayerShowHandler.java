package com.gryphpoem.game.zw.handler.client.cross.chat;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientAsyncHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb7.GetRoomPlayerShowRq;
import com.gryphpoem.game.zw.pb.GamePb7.GetRoomPlayerShowRs;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.service.CrossChatService;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-12-06 18:15
 */
public class GetRoomPlayerShowHandler extends ClientAsyncHandler {
    @Override
    public void action() throws MwException {
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        GetRoomPlayerShowRq req = msg.getExtension(GetRoomPlayerShowRq.ext);
        CrossChatService crossChatService = DataResource.ac.getBean(CrossChatService.class);
        crossChatService.getChatRoomPlayerShow(player, req.getRoomId(), req.getChlId(), req.getChatMsgId()).whenComplete(super::complete);
    }

    @Override
    public <T> void sendMsgToPlayer(T rsp) {
        sendMsgToPlayer(GetRoomPlayerShowRs.ext, (GetRoomPlayerShowRs)rsp);
    }
}
