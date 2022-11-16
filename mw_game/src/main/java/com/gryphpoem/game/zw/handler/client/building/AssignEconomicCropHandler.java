package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 分配经济作物
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/16 20:34
 */
public class AssignEconomicCropHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb1.AssignEconomicCropRq rq = msg.getExtension(GamePb1.AssignEconomicCropRq.ext);
        BuildingService buildingService = getService(BuildingService.class);
        GamePb1.AssignEconomicCropRs resp = buildingService.assignEconomicCropToResBuilding(getRoleId(), rq);
        if (null != resp) {
            sendMsgToPlayer(GamePb1.AssignEconomicCropRs.ext, resp);
        }
    }

}
