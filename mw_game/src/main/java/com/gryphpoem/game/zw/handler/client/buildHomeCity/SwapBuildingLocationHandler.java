package com.gryphpoem.game.zw.handler.client.buildHomeCity;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.buildHomeCity.BuildHomeCityService;

/**
 * 交换建筑位置
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/8 15:03
 */
public class SwapBuildingLocationHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb1.SwapBuildingLocationRq rq = msg.getExtension(GamePb1.SwapBuildingLocationRq.ext);
        BuildHomeCityService buildHomeCityService = getService(BuildHomeCityService.class);
        GamePb1.SwapBuildingLocationRs resp = buildHomeCityService.swapBuildingLocation(getRoleId(), rq);
        if (null != resp) {
            sendMsgToPlayer(GamePb1.SwapBuildingLocationRs.ext, resp);
        }
    }

}
