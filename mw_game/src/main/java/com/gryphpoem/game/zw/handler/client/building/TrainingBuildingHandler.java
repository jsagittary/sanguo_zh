package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb4.TrainingBuildingRq;
import com.gryphpoem.game.zw.pb.GamePb4.TrainingBuildingRs;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-05-31 16:19
 * @description: 建造建筑(暂时只有训练中心,需要在拆除建筑之后)
 * @modified By:
 */
public class TrainingBuildingHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        TrainingBuildingRq req = msg.getExtension(GamePb4.TrainingBuildingRq.ext);
        BuildingService buildingService = getService(BuildingService.class);
        TrainingBuildingRs resp = buildingService.trainingBuilding(getRoleId(), req.getBuildingId(), req.getBuildingType());
        sendMsgToPlayer(TrainingBuildingRs.EXT_FIELD_NUMBER, TrainingBuildingRs.ext, resp);
    }
}
