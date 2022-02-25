package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 产资源定时器
 * 
 * @author tyler
 *
 */
public class ResourceTimer extends TimerEvent {
    public ResourceTimer() {
        super(-1, 5000);
    }

    @Override
    public void action() {
        AppGameServer.ac.getBean(BuildingService.class).resourceTimerLogic();
    }

}
