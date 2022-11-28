package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;

/**
 * 人口恢复定时器
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/22 15:37
 */
public class ResidentTimer extends TimerEvent {

    public ResidentTimer() {
        super(-1, 5000);
    }

    @Override
    public void action() {
        // AppGameServer.ac.getBean(BuildHomeCityService.class).ResidentTimerLogic();
    }

}
