package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.ActivityTemplateService;

public class GetActivityDataInfoHandler extends ClientHandler {
    @Override
    public void action() throws Exception {
        GamePb4.GetActivityDataInfoRq req = this.msg.getExtension(GamePb4.GetActivityDataInfoRq.ext);
        GamePb4.GetActivityDataInfoRs resp = getService(ActivityTemplateService.class).getActivityDatainfo(getRoleId(),req.getActivityType());
        if (resp != null) {
            sendMsgToPlayer(GamePb4.GetActivityDataInfoRs.ext, resp);
        }
    }
}
