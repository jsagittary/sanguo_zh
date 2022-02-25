package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * 观看广告领取的奖励
 * @program: empire_activity
 * @description:
 * @author: zhou jie
 * @create: 2020-04-22 16:05
 */
public class AdvertisementRewardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ActivityService service = getService(ActivityService.class);
        long roleId = getRoleId();
        GamePb4.AdvertisementRewardRs resp = service.advertisementReward(roleId);
        if (null != resp) {
            sendMsgToPlayer(GamePb4.AdvertisementRewardRs.ext, resp);
        }
    }
}