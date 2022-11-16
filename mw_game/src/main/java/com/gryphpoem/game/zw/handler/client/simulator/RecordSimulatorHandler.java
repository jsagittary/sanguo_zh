package com.gryphpoem.game.zw.handler.client.simulator;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.simulator.LifeSimulatorService;

/**
 * 记录模拟器结果
 *
 * @Author: GeYuanpeng
 * @Date: 2022/10/31 10:46
 */
public class RecordSimulatorHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb1.RecordLifeSimulatorRq rq = msg.getExtension(GamePb1.RecordLifeSimulatorRq.ext);
        LifeSimulatorService lifeSimulatorService = getService(LifeSimulatorService.class);
        GamePb1.RecordLifeSimulatorRs resp = lifeSimulatorService.RecordSimulator(getRoleId(), rq);
        if (null != resp) {
            sendMsgToPlayer(GamePb1.RecordLifeSimulatorRs.ext, resp);
        }
    }

}
