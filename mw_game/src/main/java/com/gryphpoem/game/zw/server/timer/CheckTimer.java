package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.service.GameJobService;

/**
 * @author xwind
 * @date 2021/3/16
 */
public class CheckTimer extends TimerEvent {

    public CheckTimer(){
        super(-1,10000);
    }

    @Override
    public void action() throws MwException {
        DataResource.ac.getBean(GameJobService.class).checkAcrossTheDayJob();
    }
}
