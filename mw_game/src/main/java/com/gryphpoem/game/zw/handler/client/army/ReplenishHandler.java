package com.gryphpoem.game.zw.handler.client.army;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.ReplenishRq;
import com.gryphpoem.game.zw.pb.GamePb2.ReplenishRs;
import com.gryphpoem.game.zw.service.ArmyService;

/**
 * 
 * @Description 补兵
 * @author TanDonghai
 *
 */
public class ReplenishHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		ReplenishRq req = msg.getExtension(ReplenishRq.ext);
		ArmyService armyService = getService(ArmyService.class);
		ReplenishRs resp = armyService.replenish(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(ReplenishRs.ext, resp);
		}
	}

}
