package com.gryphpoem.game.zw.handler.client.friend;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.DelBlackListRq;
import com.gryphpoem.game.zw.pb.GamePb4.DelBlackListRs;
import com.gryphpoem.game.zw.service.FriendService;

/**
 * @ClassName DelBlackListHandler.java
 * @Description 删除黑名单
 * @author QiuKun
 * @date 2018年8月11日
 */
public class DelBlackListHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        DelBlackListRq req = msg.getExtension(DelBlackListRq.ext);
        FriendService service = getService(FriendService.class);
        DelBlackListRs resp = service.delBlackList(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(DelBlackListRs.ext, resp);
        }
    }

}
