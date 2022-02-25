package com.gryphpoem.game.zw.handler.client.equip;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.EquipGainRs;
import com.gryphpoem.game.zw.service.EquipService;

/**
 * 
 * @Description 装备打造领取
 * @author tyler
 *
 */
public class EquipGainHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		EquipService equipService = getService(EquipService.class);
		EquipGainRs resp = equipService.equipGain(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(EquipGainRs.ext, resp);
		}
	}
}
