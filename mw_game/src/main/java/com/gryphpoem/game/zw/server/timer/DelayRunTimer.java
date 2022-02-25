package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.manager.DressUpDataManager;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.CityService;

/**
 * 延时队列定时器
 * @description:
 * @author: zhou jie
 * @time: 2021/3/15 15:59
 */
public class DelayRunTimer extends TimerEvent {

    public DelayRunTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() throws MwException {
        DressUpDataManager dressUpDataManager = AppGameServer.ac.getBean(DressUpDataManager.class);
        dressUpDataManager.runSec();
        //处理阵营城池撤离
        CityService cityService = AppGameServer.ac.getBean(CityService.class);
        cityService.runSec();
    }

}
