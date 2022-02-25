package com.gryphpoem.game.zw.handler.client.party;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyLogRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyLogRs;
import com.gryphpoem.game.zw.service.CampService;

/**
 * 
 * @Description 获取军团日志信息
 * @author TanDonghai
 *
 */
public class GetPartyLogHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetPartyLogRq req = msg.getExtension(GetPartyLogRq.ext);
		CampService campService = getService(CampService.class);
		GetPartyLogRs resp = campService.getPartyLog(getRoleId(), req.getPage());

		if (null != resp) {
			sendMsgToPlayer(GetPartyLogRs.ext, resp);
		}
	}

}
