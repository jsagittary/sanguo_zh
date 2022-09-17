package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.manager.MailReportDataManager;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-17 14:58
 */
public class RemoveMailReportTimer extends TimerEvent {
    public RemoveMailReportTimer() {
        super(-1, 3000);
    }

    @Override
    public void action() throws Exception {
        DataResource.ac.getBean(MailReportDataManager.class).runSec();
    }
}
