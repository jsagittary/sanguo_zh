package com.gryphpoem.game.zw.handler.client.friend;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetMasterApprenticeRs;
import com.gryphpoem.game.zw.service.FriendService;

/**
 * @ClassName GetMasterApprenticeHandler.java
 * @Description 获取师徒信息
 * @author QiuKun
 * @date 2017年7月1日
 */
public class GetMasterApprenticeHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        FriendService service = getService(FriendService.class);
        long roleId = getRoleId();
        GetMasterApprenticeRs resp = service.getMasterApprentice(roleId);

        if (null != resp) {
            sendMsgToPlayer(GetMasterApprenticeRs.ext, resp);
        }
    }

}
