package com.gryphpoem.game.zw.handler.client.chat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetFmsGelTunChatsRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetFmsGelTunChatsRs;
import com.gryphpoem.game.zw.service.ChatService;

/**
 * 
* @ClassName: GetFmsGelTunChatsHandler
* @Description: 获取名将转盘最新的推送消息
* @author chenqi
* @date 2018年9月25日
*
 */
public class GetFmsGelTunChatsHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
    	GetFmsGelTunChatsRq req = msg.getExtension(GetFmsGelTunChatsRq.ext);
        ChatService chatService = getService(ChatService.class);
        GetFmsGelTunChatsRs resp = chatService.getFmsGelTunChats(getRoleId(), req.getChatId());
        if (null != resp) {
            sendMsgToPlayer(GetFmsGelTunChatsRs.ext, resp);
        }
    }

}
