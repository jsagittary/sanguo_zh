package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.GoldenAutumnFarmService;

public class EmpireFarmOpenTreasureChestHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.EmpireFarmOpenTreasureChestRq req = this.msg.getExtension(GamePb4.EmpireFarmOpenTreasureChestRq.ext);
        GamePb4.EmpireFarmOpenTreasureChestRs resp = getService(GoldenAutumnFarmService.class).handlerOpenTreasureChest(getRoleId(), req);
        if (resp != null) {
            sendMsgToPlayer(GamePb4.EmpireFarmOpenTreasureChestRs.ext, resp);
        }
    }
}
