package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.buildHomeCity.BuildHomeCityService;

/**
 * 自动清除过期土匪的定时器
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/30 16:30
 */
public class AutoDelBanditTimer extends TimerEvent {

    public AutoDelBanditTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() {
        // 建筑升级定时任务
        AppGameServer.ac.getBean(BuildHomeCityService.class).autoDelBanditTimerLogic();
    }

}
