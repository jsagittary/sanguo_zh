package com.gryphpoem.game.zw.handler.client.buildHomeCity;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.buildHomeCity.BuildHomeCityService;

/**
 * 探索迷雾
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/8 14:56
 */
public class ExploreHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb1.ExploreRq rq = msg.getExtension(GamePb1.ExploreRq.ext);
        BuildHomeCityService buildHomeCityService = getService(BuildHomeCityService.class);
        GamePb1.ExploreRs resp = buildHomeCityService.exploreFog(getRoleId(), rq);
        if (null != resp) {
            sendMsgToPlayer(GamePb1.ExploreRs.ext, resp);
        }
    }

}
