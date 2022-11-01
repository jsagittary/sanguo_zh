package com.gryphpoem.game.zw.handler.client.simulator;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.simulator.LifeSimulatorService;

/**
 * @Author: GeYuanpeng
 * @Date: 2022/10/31 16:16
 */
public class GetSimulatorInfoHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        LifeSimulatorService lifeSimulatorService = getService(LifeSimulatorService.class);
        GamePb1.GetLifeSimulatorInfoRs resp = lifeSimulatorService.getSimulatorInfo(getRoleId());
        if (null != resp) {
            sendMsgToPlayer(GamePb1.GetLifeSimulatorInfoRs.ext, resp);
        }
    }

}
