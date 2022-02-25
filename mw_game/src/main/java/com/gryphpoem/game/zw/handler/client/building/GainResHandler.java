package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GainResRq;
import com.gryphpoem.game.zw.pb.GamePb1.GainResRs;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 领取资源
 * 
 * @author tyler
 *
 */
public class GainResHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GainResRq req = msg.getExtension(GainResRq.ext);
		BuildingService buildingService = getService(BuildingService.class);
		GainResRs resp = buildingService.gainResRs(getRoleId(), req.getId());
		sendMsgToPlayer(GainResRs.EXT_FIELD_NUMBER, GainResRs.ext, resp);
	}
}
