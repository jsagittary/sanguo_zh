package com.gryphpoem.game.zw.handler.client.equip;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetEquipsRs;
import com.gryphpoem.game.zw.service.EquipService;

/**
 * 
 * @Description 获取玩家所有装备
 * @author TanDonghai
 *
 */
public class GetEquipsHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		EquipService equipService = getService(EquipService.class);
		GetEquipsRs resp = equipService.getEquips(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(GetEquipsRs.ext, resp);
		}
	}

}
