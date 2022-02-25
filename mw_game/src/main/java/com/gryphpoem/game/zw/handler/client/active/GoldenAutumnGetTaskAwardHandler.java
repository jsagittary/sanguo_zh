package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.GoldenAutumnFruitfulService;
import com.gryphpoem.game.zw.service.activity.GoldenAutumnSunriseService;

public class GoldenAutumnGetTaskAwardHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.GoldenAutumnSunriseGetTaskAwardRq req = this.msg.getExtension(GamePb4.GoldenAutumnSunriseGetTaskAwardRq.ext);
        GamePb4.GoldenAutumnSunriseGetTaskAwardRs resp = getService(GoldenAutumnSunriseService.class).receiveSingleTaskAward(getRoleId(), req);
        if (resp != null) {
            sendMsgToPlayer(GamePb4.GoldenAutumnSunriseGetTaskAwardRs.ext, resp);
        }
    }
}
