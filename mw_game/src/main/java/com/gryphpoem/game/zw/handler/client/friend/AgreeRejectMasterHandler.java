package com.gryphpoem.game.zw.handler.client.friend;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.FriendService;

/**
 * User:        zhoujie
 * Date:        2020/2/12 15:10
 * Description: 同意或者拒绝收徒
 */
public class AgreeRejectMasterHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.AgreeRejectMasterRq req = msg.getExtension(GamePb4.AgreeRejectMasterRq.ext);
        FriendService service = getService(FriendService.class);
        long roleId = getRoleId();
        GamePb4.AgreeRejectMasterRs resp = service.agreeRejectMaster(roleId, req);

        if (null != resp) {
            sendMsgToPlayer(GamePb4.AgreeRejectMasterRs.ext, resp);
        }
    }
}
