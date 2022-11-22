package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.UpBuildingRq;
import com.gryphpoem.game.zw.pb.GamePb1.UpBuildingRs;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 建筑升级
 * 
 * @author tyler
 *
 */
public class UpBuildingHanlder extends ClientHandler {

    @Override
    public void action() throws MwException {
        UpBuildingRq req = msg.getExtension(UpBuildingRq.ext);
        BuildingService buildingService = getService(BuildingService.class);
        boolean immediate = req.getImmediate();
        UpBuildingRs resp = buildingService.upgradeBuilding(getRoleId(), req.getId(), immediate);
        sendMsgToPlayer(UpBuildingRs.EXT_FIELD_NUMBER, UpBuildingRs.ext, resp);
    }
}
