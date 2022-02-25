package com.gryphpoem.game.zw.handler.client.factory;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.FactoryRecruitRq;
import com.gryphpoem.game.zw.pb.GamePb1.FactoryRecruitRs;
import com.gryphpoem.game.zw.service.FactoryService;

/**
 * 兵营信息
 * 
 * @author tyler
 *
 */
public class FactoryRecruitHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		FactoryRecruitRq req = msg.getExtension(FactoryRecruitRq.ext);
		FactoryService fctoryService = getService(FactoryService.class);
		FactoryRecruitRs resp = fctoryService.getFactoryRecruitRs(getRoleId(), req.getId(), req.getAddTime());
		sendMsgToPlayer(FactoryRecruitRs.EXT_FIELD_NUMBER, FactoryRecruitRs.ext, resp);
	}
}
