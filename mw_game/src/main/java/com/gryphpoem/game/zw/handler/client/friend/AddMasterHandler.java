package com.gryphpoem.game.zw.handler.client.friend;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.AddMasterRq;
import com.gryphpoem.game.zw.pb.GamePb4.AddMasterRs;
import com.gryphpoem.game.zw.service.FriendService;

/**
 * @ClassName AddMasterHandler.java
 * @Description 拜师
 * @author QiuKun
 * @date 2017年7月1日
 */
public class AddMasterHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        AddMasterRq req = msg.getExtension(AddMasterRq.ext);
        FriendService service = getService(FriendService.class);
        long roleId = getRoleId();
        AddMasterRs resp = service.addMasterRs(roleId, req.getMasterId());

        if (null != resp) {
            sendMsgToPlayer(AddMasterRs.ext, resp);
        }
    }

}
