package com.gryphpoem.game.zw.handler.client.party;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyJobRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyJobRs;
import com.gryphpoem.game.zw.service.CampService;

/**
 * 
 * @Description 获取军团官员信息
 * @author TanDonghai
 *
 */
public class GetPartyJobHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
	    GetPartyJobRq req = msg.getExtension(GetPartyJobRq.ext);
		CampService campService = getService(CampService.class);
		GetPartyJobRs resp = campService.getPartyJob(getRoleId(),req.getIsAppoint());

		if (null != resp) {
			sendMsgToPlayer(GetPartyJobRs.ext, resp);
		}
	}

}
