package com.gryphpoem.game.zw.handler.client.equip;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetSuperEquipRs;
import com.gryphpoem.game.zw.service.EquipService;

/**
 * 
 * @Description 获取玩家所有国器
 * @author Tyler
 *
 */
public class GetSuperEquipHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		EquipService equipService = getService(EquipService.class);
		GetSuperEquipRs resp = equipService.getSuperEquip(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(GetSuperEquipRs.ext, resp);
		}
	}

}
