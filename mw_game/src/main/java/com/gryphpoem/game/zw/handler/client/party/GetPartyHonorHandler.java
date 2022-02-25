package com.gryphpoem.game.zw.handler.client.party;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyHonorRs;
import com.gryphpoem.game.zw.service.CampService;

/**
 * 
 * @Description 获取军团荣誉数据
 * @author TanDonghai
 *
 */
public class GetPartyHonorHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		CampService campService = getService(CampService.class);
		GetPartyHonorRs resp = campService.getPartyHonor(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(GetPartyHonorRs.ext, resp);
		}
	}

}
