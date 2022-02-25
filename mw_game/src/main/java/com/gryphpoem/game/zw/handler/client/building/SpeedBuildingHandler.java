package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.SpeedBuildingRq;
import com.gryphpoem.game.zw.pb.GamePb1.SpeedBuildingRs;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 建筑加速
 *
 * @author tyler
 */
public class SpeedBuildingHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        SpeedBuildingRq req = msg.getExtension(SpeedBuildingRq.ext);
        BuildingService buildingService = getService(BuildingService.class);
        SpeedBuildingRs resp = buildingService.speedBuilding(getRoleId(), req.getId(), req.getItemId(), req.getIsGoldSpeed(), req.getItemNum());
        sendMsgToPlayer(SpeedBuildingRs.EXT_FIELD_NUMBER, SpeedBuildingRs.ext, resp);
    }
}
