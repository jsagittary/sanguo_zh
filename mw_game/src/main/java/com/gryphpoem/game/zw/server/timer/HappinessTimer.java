package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.buildHomeCity.BuildHomeCityService;

/**
 * 幸福度自然更新定时器
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/22 15:31
 */
public class HappinessTimer extends TimerEvent {

    public HappinessTimer() {
        super(-1, 5000);
    }

    @Override
    public void action() {
        AppGameServer.ac.getBean(BuildHomeCityService.class).HappinessTimerLogic();
    }

}
