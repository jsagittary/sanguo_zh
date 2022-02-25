package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetBuildingRs;
import com.gryphpoem.game.zw.service.BuildingService;

public class BuildingHanlder extends ClientHandler {

	@Override
	public void action() throws MwException {
		BuildingService buildingService = getService(BuildingService.class);
		GetBuildingRs resp = buildingService.getBuilding(getRoleId());
		sendMsgToPlayer(GetBuildingRs.EXT_FIELD_NUMBER, GetBuildingRs.ext, resp);
	}
}
