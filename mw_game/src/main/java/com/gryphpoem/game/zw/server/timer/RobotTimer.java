package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.robot.RobotService;

/**
 * @Description AI机器人相关定时任务
 * @author TanDonghai
 * @date 创建时间：2017年10月16日 上午9:44:56
 *
 */
public class RobotTimer extends TimerEvent {

    public RobotTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() throws MwException {
        AppGameServer.ac.getBean(RobotService.class).robotTimerLogic();
        // 给机器人分配坐标
        AppGameServer.ac.getBean(RobotService.class).robotAllotAreaLogic();
    }

}
