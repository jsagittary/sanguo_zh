package com.gryphpoem.game.zw.handler.http;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb.RobotDataRs;
import com.gryphpoem.game.zw.pb.HttpPb.BackRobotsDataRq;
import com.gryphpoem.game.zw.pb.HttpPb.RobotsCountByAreaRq;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.robot.RobotService;

/**
 * @Author: ZhouJie
 * @Date: Create in 2017-12-29 19:34
 * @Description: 获取指定区域内的数据
 * @Modified By:
 */
public class RobotsCountByAreaRqHandler extends HttpHandler {

    @Override
    public void action() throws MwException {
        RobotsCountByAreaRq req = msg.getExtension(RobotsCountByAreaRq.ext);
        int areaId = req.getAreaId();
        String marking = req.getMarking();
        int code = msg.getCode();
        BackRobotsDataRq.Builder res = BackRobotsDataRq.newBuilder();
        RobotService robotService = AppGameServer.ac.getBean(RobotService.class);
        RobotDataRs robotDataRs = robotService.getRobotCountByArea(areaId);
        res.setRobotData(robotDataRs);
        res.setMarking(marking);
        BasePb.Base.Builder base = PbHelper.createRsBase(BackRobotsDataRq.EXT_FIELD_NUMBER, BackRobotsDataRq.ext, res.build());
        // 发送数据到账号服
        AppGameServer.getInstance().sendMsgToPublic(base);
    }
}
