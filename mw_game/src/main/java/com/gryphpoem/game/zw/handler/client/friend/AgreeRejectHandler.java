package com.gryphpoem.game.zw.handler.client.friend;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.AgreeRejectRq;
import com.gryphpoem.game.zw.pb.GamePb4.AgreeRejectRs;
import com.gryphpoem.game.zw.service.FriendService;

/**
 * @ClassName AgreeRejectHandler.java
 * @Description 同意或拒绝添加好友
 * @author QiuKun
 * @date 2017年6月28日
 */
public class AgreeRejectHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        AgreeRejectRq req = msg.getExtension(AgreeRejectRq.ext);
        FriendService service = getService(FriendService.class);
        long roleId = getRoleId();
        AgreeRejectRs resp = service.agreeReject(roleId, req) ;

        if (null != resp) {
            sendMsgToPlayer(AgreeRejectRs.ext, resp);
        }        
    }

}
