package com.gryphpoem.game.zw.handler.client.chat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetDialogRs;
import com.gryphpoem.game.zw.service.ChatService;

/**
 * @ClassName GetDialogHandler.java
 * @Description 获取会话列表
 * @author QiuKun
 * @date 2017年9月16日
 */
public class GetDialogHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // GetDialogRq req = msg.getExtension(GetDialogRq.ext);
        ChatService chatService = getService(ChatService.class);
        GetDialogRs resp = chatService.getDialog(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(GetDialogRs.ext, resp);
        }
    }

}
