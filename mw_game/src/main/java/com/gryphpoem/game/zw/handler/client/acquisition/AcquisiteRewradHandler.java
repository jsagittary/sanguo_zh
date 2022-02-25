package com.gryphpoem.game.zw.handler.client.acquisition;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.AcquisiteRewradRq;
import com.gryphpoem.game.zw.pb.GamePb4.AcquisiteRewradRs;
import com.gryphpoem.game.zw.service.AcquisitionService;

/**
 * 
 * @Description 领取个人资源点奖励
 * @author TanDonghai
 *
 */
public class AcquisiteRewradHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		AcquisiteRewradRq req = msg.getExtension(AcquisiteRewradRq.ext);
		AcquisitionService acquisitionService = getService(AcquisitionService.class);
		AcquisiteRewradRs resp = acquisitionService.acquisiteRewrad(getRoleId(), req.getId());

		if (null != resp) {
			sendMsgToPlayer(AcquisiteRewradRs.ext, resp);
		}
	}

}
