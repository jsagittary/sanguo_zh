package com.gryphpoem.game.zw.handler.client.buildHomeCity;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.buildHomeCity.BuildHomeCityService;

/**
 * 清剿土匪
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/30 15:59
 */
public class ClearBanditHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb1.ClearBanditRq rq = msg.getExtension(GamePb1.ClearBanditRq.ext);
        BuildHomeCityService buildHomeCityService = getService(BuildHomeCityService.class);
        GamePb1.ClearBanditRs resp = buildHomeCityService.clearBandit(getRoleId(), rq);
        if (null != resp) {
            sendMsgToPlayer(GamePb1.ClearBanditRs.ext, resp);
        }
    }

}
