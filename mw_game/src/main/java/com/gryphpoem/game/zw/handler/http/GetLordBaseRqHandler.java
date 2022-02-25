package com.gryphpoem.game.zw.handler.http;

import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.HttpPb.GetLordBaseRq;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.GmToolService;

import java.util.List;

/**
 * 后台获取玩家信息
 */
public class GetLordBaseRqHandler extends HttpHandler {

    @Override
    public void action() {
        GetLordBaseRq req = msg.getExtension(GetLordBaseRq.ext);
        getLordBase(req);
    }

    public void getLordBase(final GetLordBaseRq req) {
        String marking = req.getMarking();
        long lordId = req.getLordId();
        int type = req.getType();
        List<String> params = req.getParamList();
        GmToolService toolService = AppGameServer.ac.getBean(GmToolService.class);
        Base.Builder baseBuilder = toolService.backLordBaseLogic(marking, lordId, type, params);
        AppGameServer.getInstance().sendMsgToPublic(baseBuilder, 1);
        // AppGameServer.getInstance().mainLogicServer.addCommand(new ICommand() {
        // @Override
        // public void action() {
        // }
        // }, DealType.MAIN);
    }
}
