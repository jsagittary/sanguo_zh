package com.gryphpoem.game.zw.handler.client.buildHomeCity;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 交换建筑位置
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/8 15:03
 */
public class SwapBuildingPosHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb1.SwapBuildingPosRq rq = msg.getExtension(GamePb1.SwapBuildingPosRq.ext);
        BuildingService buildingService = getService(BuildingService.class);
        GamePb1.SwapBuildingPosRs resp = buildingService.swapBuildingPos(getRoleId(), rq);
        if (null != resp) {
            sendMsgToPlayer(GamePb1.SwapBuildingPosRs.ext, resp);
        }
    }

}
