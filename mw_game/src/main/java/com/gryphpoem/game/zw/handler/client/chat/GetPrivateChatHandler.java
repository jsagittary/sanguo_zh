package com.gryphpoem.game.zw.handler.client.chat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetPrivateChatRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetPrivateChatRs;
import com.gryphpoem.game.zw.service.ChatService;

/**
 * @ClassName GetPrivateChatHandler.java
 * @Description 获取私聊信息
 * @author QiuKun
 * @date 2017年9月16日
 */
public class GetPrivateChatHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetPrivateChatRq req = msg.getExtension(GetPrivateChatRq.ext);
        ChatService chatService = getService(ChatService.class);
        GetPrivateChatRs resp = chatService.getPrivateChat(getRoleId(), req.getTargetId());

        if (null != resp) {
            sendMsgToPlayer(GetPrivateChatRs.ext, resp);
        }
    }

}
