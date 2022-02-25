package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.GestapoService;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-04-13 17:14
 * @Description:
 * @Modified By:
 */
public class GestapoTimer extends TimerEvent {

    public GestapoTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() throws MwException {
        // 移除过期的盖世太保
        AppGameServer.ac.getBean(GestapoService.class).gestapoTimerLogic();
        // 清除过期的任务
        // AppGameServer.ac.getBean(RoyalArenaService.class).personTaskLogic();

    }
}
