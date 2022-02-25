package com.gryphpoem.game.zw.handler.client.equip;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.SpeedForgeRq;
import com.gryphpoem.game.zw.pb.GamePb1.SpeedForgeRs;
import com.gryphpoem.game.zw.service.EquipService;

/**
 * 
 * @Description 装备打造加速
 * @author TanDonghai
 *
 */
public class SpeedForgeHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		SpeedForgeRq req = msg.getExtension(SpeedForgeRq.ext);
		EquipService equipService = getService(EquipService.class);
		SpeedForgeRs resp = equipService.speedForge(getRoleId(), req.getType());

		if (null != resp) {
			sendMsgToPlayer(SpeedForgeRs.ext, resp);
		}
	}
}
