package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 经济作物收取定时器
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/30 8:45
 */
public class GainEconomicCropTimer extends TimerEvent {

    public GainEconomicCropTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() throws Exception {
        AppGameServer.ac.getBean(BuildingService.class).gainEconomicCrop();
    }
}
