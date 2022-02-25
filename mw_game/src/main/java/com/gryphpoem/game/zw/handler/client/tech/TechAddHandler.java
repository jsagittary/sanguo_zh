package com.gryphpoem.game.zw.handler.client.tech;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.TechAddRq;
import com.gryphpoem.game.zw.pb.GamePb1.TechAddRs;
import com.gryphpoem.game.zw.service.TechService;

/**
 * 科技馆雇佣
 * 
 * @author tyler
 *
 */
public class TechAddHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		TechAddRq req = msg.getExtension(TechAddRq.ext);
		TechService buildingService = getService(TechService.class);
		TechAddRs resp = buildingService.doTechAdd(getRoleId(), req.getId());
		sendMsgToPlayer(TechAddRs.EXT_FIELD_NUMBER, TechAddRs.ext, resp);
	}
}
