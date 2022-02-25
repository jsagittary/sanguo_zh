package com.gryphpoem.game.zw.handler.client.friend;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.DelFriendRq;
import com.gryphpoem.game.zw.pb.GamePb4.DelFriendRs;
import com.gryphpoem.game.zw.service.FriendService;

/**
 * @ClassName DelFriendHandler.java
 * @Description 删除好友
 * @author QiuKun
 * @date 2017年6月28日
 */
public class DelFriendHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        DelFriendRq req = msg.getExtension(DelFriendRq.ext);
        FriendService service = getService(FriendService.class);
        long roleId = getRoleId();
        DelFriendRs resp = service.delFriend(roleId, req.getFriendId());

        if (null != resp) {
            sendMsgToPlayer(DelFriendRs.ext, resp);
        }
    }

}
