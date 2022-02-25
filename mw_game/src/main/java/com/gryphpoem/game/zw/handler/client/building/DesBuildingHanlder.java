package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.DesBuildingRq;
import com.gryphpoem.game.zw.pb.GamePb1.DesBuildingRs;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 建筑拆除
 * @author tyler
 *
 */
public class DesBuildingHanlder extends ClientHandler {

	@Override
	public void action() throws MwException {
		DesBuildingRq req = msg.getExtension(DesBuildingRq.ext);
		BuildingService buildingService = getService(BuildingService.class);
		DesBuildingRs resp = buildingService.desBuilding(req.getId(), getRoleId());
		sendMsgToPlayer(DesBuildingRs.EXT_FIELD_NUMBER, DesBuildingRs.ext, resp);
	}
}
