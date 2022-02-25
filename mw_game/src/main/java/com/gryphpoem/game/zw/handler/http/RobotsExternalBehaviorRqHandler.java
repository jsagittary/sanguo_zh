package com.gryphpoem.game.zw.handler.http;

import com.gryphpoem.game.zw.core.ICommand;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.pb.HttpPb.RobotsExternalBehaviorRq;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.robot.RobotService;

/**
 * @Author: ZhouJie
 * @Date: Create in 2017-12-19 12:02
 * @Description: 机器人外在行为控制
 * @Modified By:
 */
public class RobotsExternalBehaviorRqHandler extends HttpHandler {

    @Override
    public void action() throws MwException {
        RobotsExternalBehaviorRq req = msg.getExtension(RobotsExternalBehaviorRq.ext);
        externalBehaviorHandler(req, this);
    }

    public void externalBehaviorHandler(final RobotsExternalBehaviorRq req, final HttpHandler handler) {
        AppGameServer.getInstance().mainLogicServer.addCommand(new ICommand() {
            @Override
            public void action() {
                int serverId = req.getServerId();
                int robotCount = req.getRobotCount();
                int type = req.getType();
                RobotService robotService = AppGameServer.ac.getBean(RobotService.class);
                if (type == 1) {
                    robotService.gmOpenRobotsExternalBehavior(serverId, robotCount);
                } else if (type == 2) {
                    robotService.gmCloseRobotsExternalBehavior(serverId, robotCount);
                }
            }
        }, DealType.PUBLIC);

    }
}
