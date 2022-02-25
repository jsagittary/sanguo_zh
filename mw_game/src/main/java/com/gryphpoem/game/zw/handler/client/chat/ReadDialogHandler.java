package com.gryphpoem.game.zw.handler.client.chat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.ReadDialogRq;
import com.gryphpoem.game.zw.pb.GamePb3.ReadDialogRs;
import com.gryphpoem.game.zw.service.ChatService;

/**
 * @ClassName ReadDialogHandler.java
 * @Description 已读会话
 * @author QiuKun
 * @date 2017年9月16日
 */
public class ReadDialogHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ReadDialogRq req = msg.getExtension(ReadDialogRq.ext);
        ChatService chatService = getService(ChatService.class);
        ReadDialogRs resp = chatService.readDialog(getRoleId(), req.getTargetId());

        if (null != resp) {
            sendMsgToPlayer(ReadDialogRs.ext, resp);
        }
    }

}
