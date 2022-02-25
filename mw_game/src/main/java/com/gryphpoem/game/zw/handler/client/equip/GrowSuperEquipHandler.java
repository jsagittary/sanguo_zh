package com.gryphpoem.game.zw.handler.client.equip;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GrowSuperEquipRq;
import com.gryphpoem.game.zw.pb.GamePb1.GrowSuperEquipRs;
import com.gryphpoem.game.zw.service.EquipService;

/**
 * 
 * @Description 国器进阶
 * @author Tyler
 *
 */
public class GrowSuperEquipHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GrowSuperEquipRq req = msg.getExtension(GrowSuperEquipRq.ext);
		EquipService equipService = getService(EquipService.class);
		GrowSuperEquipRs resp = equipService.growSuperEquip(getRoleId(), req.getType());

		if (null != resp) {
			sendMsgToPlayer(GrowSuperEquipRs.ext, resp);
		}
	}

}
