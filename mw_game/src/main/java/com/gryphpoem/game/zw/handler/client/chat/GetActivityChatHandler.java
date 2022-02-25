package com.gryphpoem.game.zw.handler.client.chat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetActivityChatRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetActivityChatRs;
import com.gryphpoem.game.zw.service.ChatService;

/**
 * 
 * @Description 获取活动系统消息
 * @author shi.pei
 *
 */
public class GetActivityChatHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetActivityChatRq req = msg.getExtension(GetActivityChatRq.ext);
        ChatService chatService = getService(ChatService.class);
        GetActivityChatRs resp = chatService.getActivityChat(getRoleId(), req.getActivityId());
        if (null != resp) {
            sendMsgToPlayer(GetActivityChatRs.ext, resp);
        }
    }

}
