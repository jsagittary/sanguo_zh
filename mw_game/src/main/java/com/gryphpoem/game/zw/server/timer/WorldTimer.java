package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.*;
import com.gryphpoem.game.zw.service.relic.RelicService;

public class WorldTimer extends TimerEvent {
    public WorldTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() {
        AppGameServer.ac.getBean(WorldService.class).worldTimerLogic();

        AppGameServer.ac.getBean(MineService.class).mineCollectTimeLogic();

        AppGameServer.ac.getBean(WarService.class).batlleTimeLogic();

        AppGameServer.ac.getBean(CityService.class).cityTimeLogic();

        AppGameServer.ac.getBean(SuperMineService.class).superMineStateChangeTimer();

        AppGameServer.ac.getBean(AirshipService.class).runSecTimer();

        AppGameServer.ac.getBean(RelicService.class).gameRunSec();
    }
}
