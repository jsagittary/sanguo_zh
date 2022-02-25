package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.SummerChargeService;

public class DailyKeepRechargeGetAwardHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.DailyKeepRechargeGetAwardRq req = this.msg.getExtension(GamePb4.DailyKeepRechargeGetAwardRq.ext);
        GamePb4.DailyKeepRechargeGetAwardRs resp = getService(SummerChargeService.class).getAward(getRoleId(),req.getConfId(),req.getActType());
        if (resp != null) {
            sendMsgToPlayer(GamePb4.DailyKeepRechargeGetAwardRs.ext, resp);
        }
    }
}
