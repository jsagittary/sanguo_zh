package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.LuckyTurnplateRq;
import com.gryphpoem.game.zw.pb.GamePb3.LuckyTurnplateRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-06-07 19:18
 * @description: 幸运转盘抽奖
 * @modified By:
 */
public class LuckyTurnplateHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        LuckyTurnplateRq req = msg.getExtension(LuckyTurnplateRq.ext);
        ActivityService service = getService(ActivityService.class);
        LuckyTurnplateRs res = service.luckyTurnplate(getRoleId(), req.getId(), req.getCostType());
        if (res != null) sendMsgToPlayer(LuckyTurnplateRs.ext, res);
    }
}
