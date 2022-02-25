package com.gryphpoem.game.zw.handler.client.friend;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.AddFriendRq;
import com.gryphpoem.game.zw.pb.GamePb4.AddFriendRs;
import com.gryphpoem.game.zw.service.FriendService;

/**
 * @ClassName AddFriendHandler.java
 * @Description 添加好友
 * @author QiuKun
 * @date 2017年6月28日
 */
public class AddFriendHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        AddFriendRq req = msg.getExtension(AddFriendRq.ext);
        FriendService service = getService(FriendService.class);
        long roleId = getRoleId();
        AddFriendRs resp = service.addFriend(roleId, req.getFriendId());

        if (null != resp) {
            sendMsgToPlayer(AddFriendRs.ext, resp);
        }
    }

}
