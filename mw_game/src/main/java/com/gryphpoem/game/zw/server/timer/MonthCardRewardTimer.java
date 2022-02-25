package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.PayService;

/**
 * @ClassName MonthCardRewardTimer.java
 * @Description 月卡每日奖励定时器
 * @author QiuKun
 * @date 2017年6月27日
 */
public class MonthCardRewardTimer extends TimerEvent {

    public MonthCardRewardTimer() {
        super(-1, 60000); // 1分钟检查一次
    }

    @Override
    public void action() throws MwException {
        AppGameServer.ac.getBean(PayService.class).monthCardRewardByLogic();
    }

}
