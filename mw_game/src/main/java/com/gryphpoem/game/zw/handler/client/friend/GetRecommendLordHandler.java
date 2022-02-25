package com.gryphpoem.game.zw.handler.client.friend;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetRecommendLordRs;
import com.gryphpoem.game.zw.service.FriendService;

/**
 * @ClassName GetRecommendLordHandler.java
 * @Description 获取推荐玩家列表
 * @author QiuKun
 * @date 2018年3月13日
 */
public class GetRecommendLordHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // GetRecommendLordRq req = msg.getExtension(GetRecommendLordRq.ext);
        FriendService service = getService(FriendService.class);
        long roleId = getRoleId();
        GetRecommendLordRs resp = service.getRecommendLord(roleId);

        if (null != resp) {
            sendMsgToPlayer(GetRecommendLordRs.ext, resp);
        }
    }

}
