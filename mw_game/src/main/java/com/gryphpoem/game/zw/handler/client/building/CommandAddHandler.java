package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.CommandAddRq;
import com.gryphpoem.game.zw.pb.GamePb1.CommandAddRs;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 司令部天气仪(招募)
 * 
 * @author tyler
 *
 */
public class CommandAddHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		CommandAddRq req = msg.getExtension(CommandAddRq.ext);
		BuildingService buildingService = getService(BuildingService.class);
		CommandAddRs resp = buildingService.getCommandAddRs(getRoleId(), req.getId());
		sendMsgToPlayer(CommandAddRs.EXT_FIELD_NUMBER, CommandAddRs.ext, resp);
	}
}
