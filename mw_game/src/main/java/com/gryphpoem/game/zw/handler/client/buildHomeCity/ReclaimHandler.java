package com.gryphpoem.game.zw.handler.client.buildHomeCity;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.buildHomeCity.BuildHomeCityService;

/**
 * 开垦地基
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/8 15:00
 */
public class ReclaimHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb1.ReclaimFoundationRq rq = msg.getExtension(GamePb1.ReclaimFoundationRq.ext);
        BuildHomeCityService buildHomeCityService = getService(BuildHomeCityService.class);
        GamePb1.ReclaimFoundationRs resp = buildHomeCityService.reclaimFoundation(getRoleId(), rq);
        if (null != resp) {
            sendMsgToPlayer(GamePb1.ReclaimFoundationRs.ext, resp);
        }
    }

}
