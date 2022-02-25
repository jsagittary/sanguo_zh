package com.gryphpoem.game.zw.handler.client.tech;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.TechSpeedRq;
import com.gryphpoem.game.zw.pb.GamePb1.TechSpeedRs;
import com.gryphpoem.game.zw.service.TechService;

/**
 * 科技加速
 * 
 * @author tyler
 *
 */
public class TechSpeedHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		TechSpeedRq req = msg.getExtension(TechSpeedRq.ext);
		TechService buildingService = getService(TechService.class);
		TechSpeedRs resp = buildingService.doTechSpeed(getRoleId(), req.getType());
		sendMsgToPlayer(TechSpeedRs.EXT_FIELD_NUMBER, TechSpeedRs.ext, resp);
	}
}
