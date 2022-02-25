package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.LightningWarService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-05-15 19:39
 * @description: 闪电战相关定时任务
 * @modified By:
 */
public class LightningWarTimer extends TimerEvent {

    public LightningWarTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() throws MwException {
        // AppGameServer.ac.getBean(LightningWarService.class).sendChatLogic(); // 消息推送定时器
        AppGameServer.ac.getBean(LightningWarService.class).batlleTimeLogic(); // 特殊战斗定时器
    }
}
