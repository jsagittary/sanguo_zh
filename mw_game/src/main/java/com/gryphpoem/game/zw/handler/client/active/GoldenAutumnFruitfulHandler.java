package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.GoldenAutumnFruitfulService;

public class GoldenAutumnFruitfulHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.GoldenAutumnFruitfulRq req = this.msg.getExtension(GamePb4.GoldenAutumnFruitfulRq.ext);
        GamePb4.GoldenAutumnFruitfulRs resp = getService(GoldenAutumnFruitfulService.class).handlerFruitful(getRoleId(), req);
        if (resp != null) {
            sendMsgToPlayer(GamePb4.GoldenAutumnFruitfulRs.ext, resp);
        }
    }
}
