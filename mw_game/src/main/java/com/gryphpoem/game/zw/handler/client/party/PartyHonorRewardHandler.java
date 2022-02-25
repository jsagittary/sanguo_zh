package com.gryphpoem.game.zw.handler.client.party;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.PartyHonorRewardRq;
import com.gryphpoem.game.zw.pb.GamePb3.PartyHonorRewardRs;
import com.gryphpoem.game.zw.service.CampService;

/**
 * 
 * @Description 领取军团荣誉礼包
 * @author TanDonghai
 *
 */
public class PartyHonorRewardHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		PartyHonorRewardRq req = msg.getExtension(PartyHonorRewardRq.ext);
		CampService campService = getService(CampService.class);
		PartyHonorRewardRs resp = campService.partyHonorReward(getRoleId(), req.getIndex());

		if (null != resp) {
			sendMsgToPlayer(PartyHonorRewardRs.ext, resp);
		}
	}

}
