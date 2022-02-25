package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.AttckScheduleBossRs;
import com.gryphpoem.game.zw.service.WorldScheduleService;

/**
 * @author: ZhouJie
 * @date: Create in 2019-03-07 17:46
 * @description: 进攻世界进度boss
 * @modified By:
 */
public class AttckScheduleBossHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // AttckScheduleBossRq req = msg.getExtension(AttckScheduleBossRq.ext);
        WorldScheduleService service = getService(WorldScheduleService.class);
        AttckScheduleBossRs resp = service.attackScheduleBoss(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(AttckScheduleBossRs.ext, resp);
        }
    }
}
