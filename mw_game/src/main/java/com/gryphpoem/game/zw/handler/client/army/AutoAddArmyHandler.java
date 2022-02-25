package com.gryphpoem.game.zw.handler.client.army;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.AutoAddArmyRq;
import com.gryphpoem.game.zw.pb.GamePb1.AutoAddArmyRs;
import com.gryphpoem.game.zw.service.ArmyService;
/**
 * 自动补兵
 * @author tyler
 *
 */
public class AutoAddArmyHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		AutoAddArmyRq req = msg.getExtension(AutoAddArmyRq.ext);
		ArmyService armyService = getService(ArmyService.class);
		AutoAddArmyRs resp = armyService.autoAddArmy(getRoleId(), req.getStatus());

		if (null != resp) {
			sendMsgToPlayer(AutoAddArmyRs.ext, resp);
		}
	}

}
