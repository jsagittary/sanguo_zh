package com.gryphpoem.game.zw.handler.client.chat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetRedPacketRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetRedPacketRs;
import com.gryphpoem.game.zw.service.RedPacketService;

/**
 * @ClassName GetRedPacketHandler.java
 * @Description 获取红包详情
 * @author QiuKun
 * @date 2018年6月9日
 */
public class GetRedPacketHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetRedPacketRq req = msg.getExtension(GetRedPacketRq.ext);
        RedPacketService service = getService(RedPacketService.class);
        GetRedPacketRs resp = service.getRedPacket(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(GetRedPacketRs.ext, resp);
        }
    }

}
