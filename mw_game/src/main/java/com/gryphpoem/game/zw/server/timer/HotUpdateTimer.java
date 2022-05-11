package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.HotUpdateService;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-05-11 13:42
 */
public class HotUpdateTimer extends TimerEvent {


    public HotUpdateTimer() {
        super(-1, 10000);
    }

    @Override
    public void action() throws Exception {
        DataResource.ac.getBean(HotUpdateService.class).hotUpdateWithTimeLogic();
    }
}
