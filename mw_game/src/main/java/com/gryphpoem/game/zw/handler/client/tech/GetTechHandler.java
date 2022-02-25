package com.gryphpoem.game.zw.handler.client.tech;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetTechRs;
import com.gryphpoem.game.zw.service.TechService;

/**
 * 科技信息
 * 
 * @author tyler
 *
 */
public class GetTechHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		TechService combatService = getService(TechService.class);
		GetTechRs resp = combatService.getTech(getRoleId());
		sendMsgToPlayer(GetTechRs.EXT_FIELD_NUMBER, GetTechRs.ext, resp);
	}
}
