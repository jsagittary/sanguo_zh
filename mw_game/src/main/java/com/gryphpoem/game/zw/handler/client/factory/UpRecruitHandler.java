package com.gryphpoem.game.zw.handler.client.factory;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.UpRecruitRq;
import com.gryphpoem.game.zw.pb.GamePb1.UpRecruitRs;
import com.gryphpoem.game.zw.service.FactoryService;

/**
 * 兵营招募加时
 * 
 * @author tyler
 *
 */
public class UpRecruitHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		UpRecruitRq req = msg.getExtension(UpRecruitRq.ext);
		FactoryService fctoryService = getService(FactoryService.class);
		UpRecruitRs resp = fctoryService.getUpRecruitRs(getRoleId(), req.getId());
		sendMsgToPlayer(UpRecruitRs.EXT_FIELD_NUMBER, UpRecruitRs.ext, resp);
	}
}
