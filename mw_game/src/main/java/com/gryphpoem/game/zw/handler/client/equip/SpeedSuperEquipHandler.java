package com.gryphpoem.game.zw.handler.client.equip;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.SpeedSuperEquipRq;
import com.gryphpoem.game.zw.pb.GamePb1.SpeedSuperEquipRs;
import com.gryphpoem.game.zw.service.EquipService;

/**
 * 
 * @Description 国器加速
 * @author Tyler
 *
 */
public class SpeedSuperEquipHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		SpeedSuperEquipRq req = msg.getExtension(SpeedSuperEquipRq.ext);
		EquipService equipService = getService(EquipService.class);
		SpeedSuperEquipRs resp = equipService.speedSuperEquip(getRoleId(), req.getType());

		if (null != resp) {
			sendMsgToPlayer(SpeedSuperEquipRs.ext, resp);
		}
	}

}
