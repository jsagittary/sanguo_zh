package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.buildHomeCity.BuildHomeCityService;

/**
 * 叛军入侵定时器
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/30 18:59
 */
public class RebelInvadeTimer extends TimerEvent {
    public RebelInvadeTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() {
        AppGameServer.ac.getBean(BuildHomeCityService.class).rebelInvadeTimerLogic();
    }
}
