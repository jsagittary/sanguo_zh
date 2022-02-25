package com.gryphpoem.game.zw.handler.client.party;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.PartyVoteRq;
import com.gryphpoem.game.zw.pb.GamePb3.PartyVoteRs;
import com.gryphpoem.game.zw.service.CampService;

/**
 * 
 * @Description 军团官员选举投票
 * @author TanDonghai
 *
 */
public class PartyVoteHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		PartyVoteRq req = msg.getExtension(PartyVoteRq.ext);
		CampService campService = getService(CampService.class);
		PartyVoteRs resp = campService.partyVote(getRoleId(), req.getRoleId());

		if (null != resp) {
			sendMsgToPlayer(PartyVoteRs.ext, resp);
		}
	}

}
