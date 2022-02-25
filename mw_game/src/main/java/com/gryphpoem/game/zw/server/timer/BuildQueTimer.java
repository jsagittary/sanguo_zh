package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.BuildingService;

public class BuildQueTimer extends TimerEvent {
    public BuildQueTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() {
        // 建筑升级定时任务
        AppGameServer.ac.getBean(BuildingService.class).buildQueTimerLogic();
    }
}
