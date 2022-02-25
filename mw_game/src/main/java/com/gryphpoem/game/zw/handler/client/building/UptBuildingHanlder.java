package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.UptBuildingRq;
import com.gryphpoem.game.zw.pb.GamePb4.UptBuildingRs;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 
* @ClassName: UptBuildingHanlder
* @Description: 建筑改建
* @author chenqi
* @date 2018年8月11日
*
 */
public class UptBuildingHanlder extends ClientHandler {

    @Override
    public void action() throws MwException {
        UptBuildingRq req = msg.getExtension(UptBuildingRq.ext);
        BuildingService buildingService = getService(BuildingService.class);
        boolean immediate = req.getImmediate();
        int newType = req.getNewType();
        int keyId = req.getKeyId();
        UptBuildingRs resp = buildingService.uptBuilding(getRoleId(), req.getId(), newType, keyId, immediate);
        sendMsgToPlayer(UptBuildingRs.EXT_FIELD_NUMBER, UptBuildingRs.ext, resp);
    }
}
