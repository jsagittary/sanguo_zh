package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.ReBuildRq;
import com.gryphpoem.game.zw.pb.GamePb1.ReBuildRs;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 建筑重建
 * @author tyler
 *
 */
public class ReBuildHanlder extends ClientHandler {

	@Override
	public void action() throws MwException {
		ReBuildRq req = msg.getExtension(ReBuildRq.ext);
		BuildingService buildingService = getService(BuildingService.class);
		ReBuildRs resp = buildingService.reBuild(req.getId(),req.getType(), getRoleId());
		sendMsgToPlayer(ReBuildRs.EXT_FIELD_NUMBER, ReBuildRs.ext, resp);
	}
}
