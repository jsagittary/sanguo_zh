package com.gryphpoem.game.zw.handler.client.friend;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.AddBlackListRq;
import com.gryphpoem.game.zw.pb.GamePb4.AddBlackListRs;
import com.gryphpoem.game.zw.service.FriendService;

/**
 * @ClassName AddBlackListHandler.java
 * @Description 添加黑名单
 * @author QiuKun
 * @date 2018年8月11日
 */
public class AddBlackListHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        AddBlackListRq req = msg.getExtension(AddBlackListRq.ext);
        FriendService service = getService(FriendService.class);
        AddBlackListRs resp = service.addBlackList(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(AddBlackListRs.ext, resp);
        }
    }

}
