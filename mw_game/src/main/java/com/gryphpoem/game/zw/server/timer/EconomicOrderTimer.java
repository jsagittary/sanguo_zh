package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.economicOrder.EconomicOrderService;

/**
 * 经济订单刷新定时器
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/30 8:40
 */
public class EconomicOrderTimer extends TimerEvent {

    public EconomicOrderTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() throws Exception {
        AppGameServer.ac.getBean(EconomicOrderService.class).refreshEconomicOrder();
    }
}
