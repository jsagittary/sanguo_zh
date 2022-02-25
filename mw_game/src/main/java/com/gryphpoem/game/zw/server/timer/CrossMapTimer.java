package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.server.AppGameServer;

/**
 * @ClassName CrossMapTimer.java
 * @Description 新地图跑秒定时器
 * @author QiuKun
 * @date 2019年4月3日
 */
public class CrossMapTimer extends TimerEvent {

    public CrossMapTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() throws MwException {
        CrossWorldMapDataManager crossWorldMapDataManager = AppGameServer.ac.getBean(CrossWorldMapDataManager.class);
        crossWorldMapDataManager.runSec();
        crossWorldMapDataManager.saveTimerLogic();
    }

}
