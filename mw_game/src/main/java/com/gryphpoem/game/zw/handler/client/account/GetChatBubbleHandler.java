package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetChatBubbleRs;
import com.gryphpoem.game.zw.service.DressUpService;

/**
 * @ClassName GetChatBubbleHandler.java
 * @Description 获取玩家聊天气泡框
 * @author QiuKun
 * @date 2018年8月31日
 */
public class GetChatBubbleHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // GetChatBubbleRq req = msg.getExtension(GetChatBubbleRq.ext);
        DressUpService service = getService(DressUpService.class);
        GetChatBubbleRs resp = service.getChatBubble(getRoleId());
        if (resp != null) sendMsgToPlayer(GetChatBubbleRs.ext, resp);
    }

}
