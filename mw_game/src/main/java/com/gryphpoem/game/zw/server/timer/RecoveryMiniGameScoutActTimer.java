package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.buildHomeCity.BuildHomeCityService;

/**
 * 前往大世界探索小游戏的斥候体力恢复定时器
 *
 * @Author: GeYuanpeng
 * @Date: 2022/12/5 11:31
 */
public class RecoveryMiniGameScoutActTimer extends TimerEvent {

    public RecoveryMiniGameScoutActTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() {
        AppGameServer.ac.getBean(BuildHomeCityService.class).recoveryMiniGameScoutActTimeLogic();
    }

}
