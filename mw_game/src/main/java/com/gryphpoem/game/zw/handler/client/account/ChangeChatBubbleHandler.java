package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.ChangeChatBubbleRq;
import com.gryphpoem.game.zw.pb.GamePb4.ChangeChatBubbleRs;
import com.gryphpoem.game.zw.service.PlayerService;

/**
 * @ClassName ChangeChatBubbleHandler.java
 * @Description 修改聊天气泡框
 * @author QiuKun
 * @date 2018年8月31日
 */
public class ChangeChatBubbleHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ChangeChatBubbleRq req = msg.getExtension(ChangeChatBubbleRq.ext);
        PlayerService service = getService(PlayerService.class);
        ChangeChatBubbleRs resp = service.changeChatBubble(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(ChangeChatBubbleRs.ext, resp);
    }

}
