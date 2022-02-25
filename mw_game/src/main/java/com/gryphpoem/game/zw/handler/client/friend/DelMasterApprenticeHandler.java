package com.gryphpoem.game.zw.handler.client.friend;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.DelMasterApprenticeRq;
import com.gryphpoem.game.zw.pb.GamePb4.DelMasterApprenticeRs;
import com.gryphpoem.game.zw.service.FriendService;

/**
 * 解除师徒关系
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2020-11-12 17:23
 */
public class DelMasterApprenticeHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        DelMasterApprenticeRq req = msg.getExtension(DelMasterApprenticeRq.ext);
        FriendService service = getService(FriendService.class);
        long roleId = getRoleId();
        DelMasterApprenticeRs resp = service.delMasterApprentice(roleId, req.getRoleId());

        if (null != resp) {
            sendMsgToPlayer(DelMasterApprenticeRs.ext, resp);
        }
    }

}