package com.gryphpoem.game.zw.handler.client.equip;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.BagExpandRs;
import com.gryphpoem.game.zw.service.EquipService;

/**
 * 
 * @Description 装备扩容
 * @author tyler
 *
 */
public class BagExpandHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		EquipService equipService = getService(EquipService.class);
		BagExpandRs resp = equipService.bagExpand(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(BagExpandRs.ext, resp);
		}
	}

}
