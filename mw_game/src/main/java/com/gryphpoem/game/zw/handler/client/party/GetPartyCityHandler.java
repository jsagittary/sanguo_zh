package com.gryphpoem.game.zw.handler.client.party;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyCityRs;
import com.gryphpoem.game.zw.service.CampService;

/**
 * 
 * @Description 获取军团城池信息
 * @author TanDonghai
 *
 */
public class GetPartyCityHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		CampService campService = getService(CampService.class);
		GetPartyCityRs resp = campService.getPartyCity(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(GetPartyCityRs.ext, resp);
		}
	}

}
