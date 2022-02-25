package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetTriggerGiftRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetTriggerGiftRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-03-03 15:05
 * @Description: 获取触发式礼包
 * @Modified By:
 */
public class GetTriggerGiftHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetTriggerGiftRq req = msg.getExtension(GetTriggerGiftRq.ext);
        GetTriggerGiftRs res = getService(ActivityService.class).GetTriggerGift(req, getRoleId());
        sendMsgToPlayer(GetTriggerGiftRs.EXT_FIELD_NUMBER, GetTriggerGiftRs.ext, res);
    }
}
