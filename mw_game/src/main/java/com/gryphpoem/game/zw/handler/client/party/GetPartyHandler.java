package com.gryphpoem.game.zw.handler.client.party;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyRs;
import com.gryphpoem.game.zw.service.CampService;

/**
 * 
 * @Description 获取军团信息
 * @author TanDonghai
 *
 */
public class GetPartyHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		CampService campService = getService(CampService.class);
		GetPartyRs resp = campService.getCampInfo(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(GetPartyRs.ext, resp);
		}
	}

}
