package com.gryphpoem.game.zw.handler.client.army;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetArmyRs;
import com.gryphpoem.game.zw.service.ArmyService;

/**
 * 
 * @Description 获取行军队列
 * @author TanDonghai
 *
 */
public class GetArmyHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		ArmyService armyService = getService(ArmyService.class);
		GetArmyRs resp = armyService.getArmy(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(GetArmyRs.ext, resp);
		}
	}

}
