package com.gryphpoem.game.zw.handler.client.equip;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.OnEquipRq;
import com.gryphpoem.game.zw.pb.GamePb1.OnEquipRs;
import com.gryphpoem.game.zw.service.EquipService;

/**
 * 
 * @Description 穿戴、卸下装备
 * @author TanDonghai
 *
 */
public class OnEquipHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		OnEquipRq req = msg.getExtension(OnEquipRq.ext);
		EquipService equipService = getService(EquipService.class);
		OnEquipRs resp = equipService.onEquip(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(OnEquipRs.ext, resp);
		}
	}

}
