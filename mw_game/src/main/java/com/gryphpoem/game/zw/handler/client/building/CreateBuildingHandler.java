package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 建造建筑
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/18 21:09
 */
public class CreateBuildingHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb1.CreateBuildingRq rq = msg.getExtension(GamePb1.CreateBuildingRq.ext);
        BuildingService buildingService = getService(BuildingService.class);
        GamePb1.CreateBuildingRs rs = buildingService.createBuilding(getRoleId(), rq);
        sendMsgToPlayer(GamePb1.CreateBuildingRs.EXT_FIELD_NUMBER, GamePb1.CreateBuildingRs.ext, rs);
    }

}