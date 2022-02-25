package com.gryphpoem.game.zw.handler.client.chat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.pb.GamePb3.GetRedPacketRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetRedPacketRs;
import com.gryphpoem.game.zw.service.RedPacketService;

/**
 * @ClassName GetRedPacketListRqHandler .java
 * @Description 获取红包详情
 */
public class GetRedPacketListRqHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb3.GetRedPacketListRq req = msg.getExtension(GamePb3.GetRedPacketListRq.ext);
        RedPacketService service = getService(RedPacketService.class);
        GamePb3.GetRedPacketListRs resp = service.getRedPacketList(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(GamePb3.GetRedPacketListRs.ext, resp);
        }
    }

}
