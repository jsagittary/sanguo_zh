package com.gryphpoem.game.zw.handler.client.friend;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetBlacklistRs;
import com.gryphpoem.game.zw.service.FriendService;

/**
 * @ClassName GetBlacklistHandler.java
 * @Description 获取黑名单
 * @author QiuKun
 * @date 2018年8月11日
 */
public class GetBlacklistHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // GetBlacklistRq req = msg.getExtension(GetBlacklistRq.ext);
        FriendService service = getService(FriendService.class);
        GetBlacklistRs resp = service.getBlacklist(getRoleId());
        if (null != resp) {
            sendMsgToPlayer(GetBlacklistRs.ext, resp);
        }

    }

}
