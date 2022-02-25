package com.gryphpoem.game.zw.handler.client.factory;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetFactoryRq;
import com.gryphpoem.game.zw.pb.GamePb1.GetFactoryRs;
import com.gryphpoem.game.zw.service.FactoryService;

/**
 * 兵营信息
 * 
 * @author tyler
 *
 */
public class GetFactoryHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetFactoryRq req = msg.getExtension(GetFactoryRq.ext);
		FactoryService fctoryService = getService(FactoryService.class);
		GetFactoryRs resp = fctoryService.getFactoryRs(getRoleId(), req.getId());
		sendMsgToPlayer(GetFactoryRs.EXT_FIELD_NUMBER, GetFactoryRs.ext, resp);
	}
}
