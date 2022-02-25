package com.gryphpoem.game.zw.handler.client.chat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.SendChatRq;
import com.gryphpoem.game.zw.pb.GamePb3.SendChatRs;
import com.gryphpoem.game.zw.service.ChatService;

/**
 * 
 * @Description 发送聊天
 * @author TanDonghai
 *
 */
public class SendChatHandler extends ClientHandler {

	@Override
	public void action() throws Exception {
		SendChatRq req = msg.getExtension(SendChatRq.ext);
		ChatService chatService = getService(ChatService.class);
		SendChatRs resp = chatService.sendChat(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(SendChatRs.ext, resp);
		}
	}

}
