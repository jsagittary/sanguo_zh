package com.gryphpoem.game.zw.handler.client.party;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.PartyBuildRs;
import com.gryphpoem.game.zw.service.CampService;

/**
 * 
 * @Description 军团建设
 * @author TanDonghai
 *
 */
public class PartyBuildHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		CampService campService = getService(CampService.class);
		PartyBuildRs resp = campService.campBuild(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(PartyBuildRs.ext, resp);
		}
	}

}
