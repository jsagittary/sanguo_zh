package com.gryphpoem.game.zw.handler.http;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.pb.HttpPb;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2020-10-27 10:09
 */
public class WeChatSignRewardHandler extends HttpHandler {

    @Override
    public void action() throws MwException {
        HttpPb.WeChatSignRewardRq req = msg.getExtension(HttpPb.WeChatSignRewardRq.ext);
        ActivityService server = AppGameServer.ac.getBean(ActivityService.class);
        server.webChatSignReward(req);
    }

}