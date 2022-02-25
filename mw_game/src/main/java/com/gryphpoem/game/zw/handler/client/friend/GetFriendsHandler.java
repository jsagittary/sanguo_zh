package com.gryphpoem.game.zw.handler.client.friend;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetFriendsRs;
import com.gryphpoem.game.zw.service.FriendService;

/**
 * @ClassName GetFriendsHandler.java
 * @Description 获取好友列表
 * @author QiuKun
 * @date 2017年6月28日
 */
public class GetFriendsHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // GetFriendsRq req = msg.getExtension(GetFriendsRq.ext);
        FriendService service = getService(FriendService.class);
        long roleId = getRoleId();
        GetFriendsRs resp = service.getFriends(roleId);

        if (null != resp) {
            sendMsgToPlayer(GetFriendsRs.ext, resp);
        }
    }

}
