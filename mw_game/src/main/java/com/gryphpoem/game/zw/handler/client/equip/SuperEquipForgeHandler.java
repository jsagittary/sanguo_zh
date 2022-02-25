package com.gryphpoem.game.zw.handler.client.equip;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.SuperEquipForgeRq;
import com.gryphpoem.game.zw.pb.GamePb1.SuperEquipForgeRs;
import com.gryphpoem.game.zw.service.EquipService;

/**
 * 
 * @Description 国器打造
 * @author Tyler
 *
 */
public class SuperEquipForgeHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		SuperEquipForgeRq req = msg.getExtension(SuperEquipForgeRq.ext);
		EquipService equipService = getService(EquipService.class);
		SuperEquipForgeRs resp = equipService.superEquipForge(getRoleId(), req.getType());

		if (null != resp) {
			sendMsgToPlayer(SuperEquipForgeRs.ext, resp);
		}
	}

}
