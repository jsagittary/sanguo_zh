package com.gryphpoem.game.zw.handler.client.tech;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.UpTechRq;
import com.gryphpoem.game.zw.pb.GamePb1.UpTechRs;
import com.gryphpoem.game.zw.service.TechService;

/**
 * 科技升级
 * 
 * @author tyler
 *
 */
public class UpTechHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		UpTechRq req = msg.getExtension(UpTechRq.ext);
		TechService buildingService = getService(TechService.class);
		UpTechRs resp = buildingService.doUpTech(getRoleId(), req.getId());
		sendMsgToPlayer(UpTechRs.EXT_FIELD_NUMBER, UpTechRs.ext, resp);
	}
}
