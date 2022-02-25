package com.gryphpoem.game.zw.handler.client.equip;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.EquipBaptizeRq;
import com.gryphpoem.game.zw.pb.GamePb1.EquipBaptizeRs;
import com.gryphpoem.game.zw.service.EquipService;

/**
 * 
 * @Description 装备洗练
 * @author tyler
 *
 */
public class EquipBaptizeHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		EquipBaptizeRq req = msg.getExtension(EquipBaptizeRq.ext);
		EquipService equipService = getService(EquipService.class);
		EquipBaptizeRs resp = equipService.equipBaptize(getRoleId(), req.getKeyId(), req.getUseGold(), req.getSuperGold());

		if (null != resp) {
			sendMsgToPlayer(EquipBaptizeRs.ext, resp);
		}
	}

}
