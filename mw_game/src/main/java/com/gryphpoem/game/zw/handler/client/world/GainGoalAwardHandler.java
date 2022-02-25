package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GainGoalAwardRq;
import com.gryphpoem.game.zw.pb.GamePb4.GainGoalAwardRs;
import com.gryphpoem.game.zw.service.WorldScheduleService;

/**
 * @author: ZhouJie
 * @date: Create in 2019-03-05 16:00
 * @description: 领取进度限时目标奖励
 * @modified By:
 */
public class GainGoalAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GainGoalAwardRq req = msg.getExtension(GainGoalAwardRq.ext);
        WorldScheduleService service = getService(WorldScheduleService.class);
        GainGoalAwardRs resp = service.gainGoalAward(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(GainGoalAwardRs.ext, resp);
        }
    }
}
