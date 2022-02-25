package com.gryphpoem.game.zw.handler.http;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.pb.HttpPb;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @program: server
 * @description:
 * @author: zhou jie
 * @create: 2019-11-25 16:44
 */
public class ShareRewardRsHandler extends HttpHandler {

    @Override
    public void action() throws MwException {
        HttpPb.ShareRewardRs req = msg.getExtension(HttpPb.ShareRewardRs.ext);
        ActivityService server = AppGameServer.ac.getBean(ActivityService.class);
        server.shareRewardRs(req);
    }
}