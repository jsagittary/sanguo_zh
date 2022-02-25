package com.gryphpoem.game.zw.handler.client.shop;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.ChatBubbleBuyRq;
import com.gryphpoem.game.zw.pb.GamePb3.ChatBubbleBuyRs;
import com.gryphpoem.game.zw.service.ShopService;

/**
 * @ClassName ChatBubbleBuyHandler.java
 * @Description 聊天气泡购买
 * @author QiuKun
 * @date 2018年8月31日
 */
public class ChatBubbleBuyHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ChatBubbleBuyRq req = msg.getExtension(ChatBubbleBuyRq.ext);
        ShopService service = getService(ShopService.class);
        ChatBubbleBuyRs resp = service.changeChatBubble(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(ChatBubbleBuyRs.EXT_FIELD_NUMBER, ChatBubbleBuyRs.ext, resp);
    }

}
