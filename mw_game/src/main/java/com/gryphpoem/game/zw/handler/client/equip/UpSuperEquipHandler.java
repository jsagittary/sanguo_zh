package com.gryphpoem.game.zw.handler.client.equip;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.UpSuperEquipRq;
import com.gryphpoem.game.zw.pb.GamePb1.UpSuperEquipRs;
import com.gryphpoem.game.zw.service.EquipService;

/**
 * 
 * @Description 国器升级
 * @author Tyler
 *
 */
public class UpSuperEquipHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		UpSuperEquipRq req = msg.getExtension(UpSuperEquipRq.ext);
		EquipService equipService = getService(EquipService.class);
		UpSuperEquipRs resp = equipService.upSuperEquip(getRoleId(), req.getType());

		if (null != resp) {
			sendMsgToPlayer(UpSuperEquipRs.ext, resp);
		}
	}

}
