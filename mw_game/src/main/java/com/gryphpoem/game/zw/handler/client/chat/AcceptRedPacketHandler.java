package com.gryphpoem.game.zw.handler.client.chat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.AcceptRedPacketRq;
import com.gryphpoem.game.zw.pb.GamePb3.AcceptRedPacketRs;
import com.gryphpoem.game.zw.service.RedPacketService;

/**
 * @ClassName AcceptRedPacketHandler.java
 * @Description 领取红包
 * @author QiuKun
 * @date 2018年6月9日
 */
public class AcceptRedPacketHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        AcceptRedPacketRq req = msg.getExtension(AcceptRedPacketRq.ext);
        RedPacketService service = getService(RedPacketService.class);
        AcceptRedPacketRs resp = service.acceptRedPacket(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(AcceptRedPacketRs.ext, resp);
        }
    }

}
