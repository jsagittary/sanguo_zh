package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.MailService;

public class DelMailTimer extends TimerEvent {

    public DelMailTimer() {
        super(-1, TimeHelper.HOUR_S * 1000L);
    }

    @Override
    public void action() {
        // 判断是否到当天5点，到5点则开始执行删除过期邮件
        if (TimeHelper.getHour() == 5) {
            AppGameServer.ac.getBean(MailService.class).delExpiredMail();
        }
    }
}
