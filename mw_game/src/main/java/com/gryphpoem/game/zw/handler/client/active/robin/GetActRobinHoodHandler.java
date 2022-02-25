package com.gryphpoem.game.zw.handler.client.active.robin;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.service.activity.ActivityRobinHoodService;

/**
 * User:        zhoujie
 * Date:        2020/2/16 11:14
 * Description:
 */
public class GetActRobinHoodHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.GetActRobinHoodRq req = msg.getExtension(GamePb4.GetActRobinHoodRq.ext);
        ActivityRobinHoodService service = getService(ActivityRobinHoodService.class);
        GamePb4.GetActRobinHoodRs rs = service.getActRobinHoodHandler(getRoleId(),req.getActivityId());
        if (!CheckNull.isNull(rs)) {
            sendMsgToPlayer(GamePb4.GetActRobinHoodRs.ext, rs);
        }
    }
}
