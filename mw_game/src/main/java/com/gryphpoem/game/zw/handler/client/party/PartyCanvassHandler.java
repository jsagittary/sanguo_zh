package com.gryphpoem.game.zw.handler.client.party;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.PartyCanvassRq;
import com.gryphpoem.game.zw.pb.GamePb3.PartyCanvassRs;
import com.gryphpoem.game.zw.service.CampService;

/**
 * 
 * @Description 军团官员选举拉票
 * @author TanDonghai
 *
 */
public class PartyCanvassHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		PartyCanvassRq req = msg.getExtension(PartyCanvassRq.ext);
		CampService campService = getService(CampService.class);
		PartyCanvassRs resp = campService.partyCanvass(getRoleId(), req.getRoleId());

		if (null != resp) {
			sendMsgToPlayer(PartyCanvassRs.ext, resp);
		}
	}

}
