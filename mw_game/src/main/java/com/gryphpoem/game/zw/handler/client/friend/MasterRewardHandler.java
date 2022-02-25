package com.gryphpoem.game.zw.handler.client.friend;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.MasterRewardRq;
import com.gryphpoem.game.zw.pb.GamePb4.MasterRewardRs;
import com.gryphpoem.game.zw.service.FriendService;

/**
 * @ClassName MasterRewardHandler.java
 * @Description 领取师徒奖励
 * @author QiuKun
 * @date 2017年7月4日
 */
public class MasterRewardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        MasterRewardRq req = msg.getExtension(MasterRewardRq.ext);
        FriendService service = getService(FriendService.class);
        long roleId = getRoleId();
        MasterRewardRs resp = service.masterReward(roleId, req.getRewardId());

        if (null != resp) {
            sendMsgToPlayer(MasterRewardRs.ext, resp);
        }
    }

}
