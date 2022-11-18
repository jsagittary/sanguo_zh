package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.manager.DressUpDataManager;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.BuildingService;
import com.gryphpoem.game.zw.service.CityService;
import com.gryphpoem.game.zw.service.economicOrder.EconomicOrderService;
import com.gryphpoem.game.zw.service.simulator.LifeSimulatorService;

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
        // 刷新经济订单
        AppGameServer.ac.getBean(EconomicOrderService.class).runSec();
        // 模拟器延时处理
        AppGameServer.ac.getBean(LifeSimulatorService.class).runSec();
        // 采集产出的经济作物
        AppGameServer.ac.getBean(BuildingService.class).runSec();
    }

}
