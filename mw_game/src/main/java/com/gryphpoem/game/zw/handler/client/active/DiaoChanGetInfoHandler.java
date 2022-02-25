package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;

public class DiaoChanGetInfoHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.DiaoChanGetInfoRq req = this.msg.getExtension(GamePb4.DiaoChanGetInfoRq.ext);
        GamePb4.DiaoChanGetInfoRs resp = getService(ActivityDiaoChanService.class).getInfo(getRoleId(),req.getActivityType());
        if (resp != null) {
            sendMsgToPlayer(GamePb4.DiaoChanGetInfoRs.ext, resp);
        }
    }
}
