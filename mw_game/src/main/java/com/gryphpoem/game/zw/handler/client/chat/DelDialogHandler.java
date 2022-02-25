package com.gryphpoem.game.zw.handler.client.chat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.DelDialogRq;
import com.gryphpoem.game.zw.pb.GamePb3.DelDialogRs;
import com.gryphpoem.game.zw.service.ChatService;

/**
 * @ClassName DelDialogHandler.java
 * @Description 删除会话
 * @author QiuKun
 * @date 2017年9月16日
 */
public class DelDialogHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        DelDialogRq req = msg.getExtension(DelDialogRq.ext);
        ChatService chatService = getService(ChatService.class);
        DelDialogRs resp = chatService.delDialog(getRoleId(), req.getTargetIdList());

        if (null != resp) {
            sendMsgToPlayer(DelDialogRs.ext, resp);
        }
    }

}
