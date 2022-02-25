package com.gryphpoem.game.zw.handler.client.equip;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.EquipDecomposeRq;
import com.gryphpoem.game.zw.pb.GamePb1.EquipDecomposeRs;
import com.gryphpoem.game.zw.service.EquipService;

/**
 * 
 * @Description 装备分解
 * @author tyler
 *
 */
public class EquipDecomposeHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		EquipDecomposeRq req = msg.getExtension(EquipDecomposeRq.ext);
		EquipService equipService = getService(EquipService.class);
		EquipDecomposeRs resp = equipService.equipDecompose(getRoleId(), req.getKeyId());

		if (null != resp) {
			sendMsgToPlayer(EquipDecomposeRs.ext, resp);
		}
	}

}
