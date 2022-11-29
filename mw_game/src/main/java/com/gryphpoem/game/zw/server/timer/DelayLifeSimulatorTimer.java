package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.simulator.LifeSimulatorService;

/**
 * 模拟器延时定时器
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/30 8:44
 */
public class DelayLifeSimulatorTimer extends TimerEvent {

    public DelayLifeSimulatorTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() throws Exception {
        AppGameServer.ac.getBean(LifeSimulatorService.class).refreshDelayLifeSimulator();
    }
}
