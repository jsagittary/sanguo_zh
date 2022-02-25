package com.gryphpoem.game.zw.handler.client.factory;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.RecruitCancelRq;
import com.gryphpoem.game.zw.pb.GamePb1.RecruitCancelRs;
import com.gryphpoem.game.zw.service.FactoryService;

/**
 * 司令部天气仪(招募)
 * 
 * @author tyler
 *
 */
public class RecruitCancelHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		RecruitCancelRq req = msg.getExtension(RecruitCancelRq.ext);
		FactoryService fctoryService = getService(FactoryService.class);
		RecruitCancelRs resp = fctoryService.getRecruitCancelRs(getRoleId(), req.getId(), req.getEndTime());
		sendMsgToPlayer(RecruitCancelRs.EXT_FIELD_NUMBER, RecruitCancelRs.ext, resp);
	}
}
