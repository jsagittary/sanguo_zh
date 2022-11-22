package com.gryphpoem.game.zw.handler.client.buildHomeCity;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.buildHomeCity.BuildHomeCityService;

/**
 * 安民济物
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/22 15:15
 */
public class PeaceAndWelfareHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb1.PeaceAndWelfareRq rq = msg.getExtension(GamePb1.PeaceAndWelfareRq.ext);
        BuildHomeCityService buildHomeCityService = getService(BuildHomeCityService.class);
        GamePb1.PeaceAndWelfareRs resp = buildHomeCityService.peaceAndWelfare(getRoleId(), rq);
        if (null != resp) {
            sendMsgToPlayer(GamePb1.PeaceAndWelfareRs.ext, resp);
        }
    }

}
