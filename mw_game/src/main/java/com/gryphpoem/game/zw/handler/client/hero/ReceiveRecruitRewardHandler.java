package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.HeroService;

/**
 * 领取招募奖励
 * @description:
 * @author: zhou jie
 * @time: 2022/2/28 18:33
 */
public class ReceiveRecruitRewardHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb1.ReceiveRecruitRewardRq req = msg.getExtension(GamePb1.ReceiveRecruitRewardRq.ext);
        HeroService heroService = getService(HeroService.class);
        GamePb1.ReceiveRecruitRewardRs resp = heroService.receiveRecruitReward(getRoleId(), req.getIndex());

        if (null != resp) {
            sendMsgToPlayer(GamePb1.ReceiveRecruitRewardRs.ext, resp);
        }
    }
}
