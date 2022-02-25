package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetScheduleRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetScheduleRs;
import com.gryphpoem.game.zw.service.WorldScheduleService;

/**
 * @author: ZhouJie
 * @date: Create in 2019-03-05 15:59
 * @description: 获取世界进度信息
 * @modified By:
 */
public class GetScheduleHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetScheduleRq req = msg.getExtension(GetScheduleRq.ext);
        WorldScheduleService service = getService(WorldScheduleService.class);
        GetScheduleRs resp = service.getSchedule(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(GetScheduleRs.ext, resp);
        }
    }
}
