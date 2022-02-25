package com.gryphpoem.game.zw.handler.client.equip;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.EquipForgeRq;
import com.gryphpoem.game.zw.pb.GamePb1.EquipForgeRs;
import com.gryphpoem.game.zw.service.EquipService;

/**
 * 
 * @Description 装备打造
 * @author TanDonghai
 *
 */
public class EquipForgeHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		EquipForgeRq req = msg.getExtension(EquipForgeRq.ext);
		EquipService equipService = getService(EquipService.class);
		EquipForgeRs resp = equipService.equipForge(getRoleId(), req.getEquipId());

		if (null != resp) {
			sendMsgToPlayer(EquipForgeRs.ext, resp);
		}
	}

}
