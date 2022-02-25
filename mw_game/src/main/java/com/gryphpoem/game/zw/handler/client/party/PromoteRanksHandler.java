package com.gryphpoem.game.zw.handler.client.party;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.PromoteRanksRs;
import com.gryphpoem.game.zw.service.CampService;

/**
 * 
 * @Description 晋升军阶
 * @author TanDonghai
 *
 */
public class PromoteRanksHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		CampService campService = getService(CampService.class);
		PromoteRanksRs resp = campService.promoteRanks(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(PromoteRanksRs.ext, resp);
		}
	}

}
