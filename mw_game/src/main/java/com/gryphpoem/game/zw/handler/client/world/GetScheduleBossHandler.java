package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetScheduleBossRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetScheduleBossRs;
import com.gryphpoem.game.zw.service.WorldScheduleService;

/**
 * @ClassName GetScheduleBossHandler.java
 * @Description 获取世界进程boss血量
 * @author QiuKun
 * @date 2019年3月18日
 */
public class GetScheduleBossHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetScheduleBossRq req = msg.getExtension(GetScheduleBossRq.ext);
        WorldScheduleService service = getService(WorldScheduleService.class);
        GetScheduleBossRs resp = service.getScheduleBoss(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(GetScheduleBossRs.ext, resp);
        }
    }

}
