package com.gryphpoem.game.zw.handler.client.party;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyBattleRs;
import com.gryphpoem.game.zw.service.CampService;

/**
 * 
 * @Description 获取军团战争信息
 * @author TanDonghai
 *
 */
public class GetPartyBattleHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		CampService campService = getService(CampService.class);
		GetPartyBattleRs resp = campService.getPartyBattle(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(GetPartyBattleRs.ext, resp);
		}
	}

}
