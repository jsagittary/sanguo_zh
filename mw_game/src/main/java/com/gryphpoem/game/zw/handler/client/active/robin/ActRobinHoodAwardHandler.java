package com.gryphpoem.game.zw.handler.client.active.robin;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.service.activity.ActivityRobinHoodService;

/**
 * User:        zhoujie
 * Date:        2020/2/16 11:20
 * Description:
 */
public class ActRobinHoodAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.ActRobinHoodAwardRq req = msg.getExtension(GamePb4.ActRobinHoodAwardRq.ext);
        ActivityRobinHoodService service = getService(ActivityRobinHoodService.class);
        GamePb4.ActRobinHoodAwardRs rs = service.actRobinHoodAward(getRoleId(),req);
        if (!CheckNull.isNull(rs)) {
            sendMsgToPlayer(GamePb4.ActRobinHoodAwardRs.ext, rs);
        }
    }
}
