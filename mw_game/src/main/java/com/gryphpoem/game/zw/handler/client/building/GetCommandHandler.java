package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetCommandRs;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 司令部信息
 * 
 * @author tyler
 *
 */
public class GetCommandHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		BuildingService buildingService = getService(BuildingService.class);
		GetCommandRs resp = buildingService.getCommandRs(getRoleId());
		sendMsgToPlayer(GetCommandRs.EXT_FIELD_NUMBER, GetCommandRs.ext, resp);
	}
}
