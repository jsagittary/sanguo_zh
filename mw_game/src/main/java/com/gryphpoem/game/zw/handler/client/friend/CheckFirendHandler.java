package com.gryphpoem.game.zw.handler.client.friend;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.CheckFirendRq;
import com.gryphpoem.game.zw.pb.GamePb4.CheckFirendRs;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.FriendService;

/**
 * @ClassName CheckFirendHandler.java
 * @Description 查看好友
 * @author QiuKun
 * @date 2017年6月30日
 */
public class CheckFirendHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        CheckFirendRq req = msg.getExtension(CheckFirendRq.ext);
        FriendService service = AppGameServer.ac.getBean(FriendService.class);
        long roleId = getRoleId();
        CheckFirendRs resp = service.getFriendDetail(roleId, req.getFriendId());
        if (null != resp) {
            sendMsgToPlayer(CheckFirendRs.ext, resp);
        }

    }

}
