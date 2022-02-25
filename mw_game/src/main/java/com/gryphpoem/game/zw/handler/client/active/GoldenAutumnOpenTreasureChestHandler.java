package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.GoldenAutumnSunriseService;

public class GoldenAutumnOpenTreasureChestHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.GoldenAutumnSunriseOpenTreasureChestRq req = this.msg.getExtension(GamePb4.GoldenAutumnSunriseOpenTreasureChestRq.ext);
        GamePb4.GoldenAutumnSunriseOpenTreasureChestRs resp = getService(GoldenAutumnSunriseService.class).openTreasureChest(getRoleId(), req);
        if (resp != null) {
            sendMsgToPlayer(GamePb4.GoldenAutumnSunriseOpenTreasureChestRs.ext, resp);
        }
    }
}
