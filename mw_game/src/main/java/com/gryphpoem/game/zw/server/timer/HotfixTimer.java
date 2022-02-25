package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.server.HotfixService;

/**
 * @author zhangdh
 * @ClassName: HotfixTimer
 * @Description:
 * @date 2017-09-22 16:33
 */
public class HotfixTimer extends TimerEvent {
    public HotfixTimer() {
        super(-1, 10000);
    }

    @Override
    public void action() {
        AppGameServer.ac.getBean(HotfixService.class).hotfixWithTimeLogic();
    }
}
