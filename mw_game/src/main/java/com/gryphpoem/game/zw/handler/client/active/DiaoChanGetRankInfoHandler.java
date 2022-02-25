package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;

public class DiaoChanGetRankInfoHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.DiaoChanGetRankInfoRq req = msg.getExtension(GamePb4.DiaoChanGetRankInfoRq.ext);
        GamePb4.DiaoChanGetRankInfoRs resp = getService(ActivityDiaoChanService.class).getRankInfo(getRoleId(),req.getType(),req.getDay(),req.getActivityType());
        if (resp != null) {
            sendMsgToPlayer(GamePb4.DiaoChanGetRankInfoRs.ext, resp);
        }
    }
}
