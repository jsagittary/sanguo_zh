package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-05-11 13:42
 */
public class HotUpdateTimer extends TimerEvent {


    protected HotUpdateTimer() {
        super(-1, 10000);
    }

    @Override
    public void action() throws Exception {

    }
}
