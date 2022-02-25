package com.gryphpoem.game.zw.handler.client.factory;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.AddArmRq;
import com.gryphpoem.game.zw.pb.GamePb1.AddArmRs;
import com.gryphpoem.game.zw.service.FactoryService;

/**
 * 兵营领取募兵
 * 
 * @author tyler
 *
 */
public class AddArmHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		AddArmRq req = msg.getExtension(AddArmRq.ext);
		FactoryService fctoryService = getService(FactoryService.class);
		AddArmRs resp = fctoryService.getAddArmRs(getRoleId(), req.getId());
		sendMsgToPlayer(AddArmRs.EXT_FIELD_NUMBER, AddArmRs.ext, resp);
	}
}
