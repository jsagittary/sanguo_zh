package com.gryphpoem.game.zw.handler.client.chat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetChatRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetChatRs;
import com.gryphpoem.game.zw.service.ChatService;

/**
 * 
 * @Description 获取最近的聊天记录
 * @author TanDonghai
 *
 */
public class GetChatHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetChatRq req = msg.getExtension(GetChatRq.ext);
        ChatService chatService = getService(ChatService.class);
        GetChatRs resp = chatService.getChat(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(GetChatRs.ext, resp);
        }
    }

}
