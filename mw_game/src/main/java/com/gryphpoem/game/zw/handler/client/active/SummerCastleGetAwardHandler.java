package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.SummerCastleService;
import com.gryphpoem.game.zw.service.activity.SummerChargeService;

public class SummerCastleGetAwardHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.SummerCastleGetAwardRq req = this.msg.getExtension(GamePb4.SummerCastleGetAwardRq.ext);
        GamePb4.SummerCastleGetAwardRs resp = getService(SummerCastleService.class).getAward(getRoleId(),req.getConfId(),req.getActType());
        if (resp != null) {
            sendMsgToPlayer(GamePb4.SummerCastleGetAwardRs.ext, resp);
        }
    }
}
