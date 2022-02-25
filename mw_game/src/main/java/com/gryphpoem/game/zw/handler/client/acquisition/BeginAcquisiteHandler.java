package com.gryphpoem.game.zw.handler.client.acquisition;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.BeginAcquisiteRq;
import com.gryphpoem.game.zw.pb.GamePb4.BeginAcquisiteRs;
import com.gryphpoem.game.zw.service.AcquisitionService;

/**
 * 
 * @Description 个人资源点开始采集
 * @author TanDonghai
 *
 */
public class BeginAcquisiteHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		BeginAcquisiteRq req = msg.getExtension(BeginAcquisiteRq.ext);
		AcquisitionService acquisitionService = getService(AcquisitionService.class);
		BeginAcquisiteRs resp = acquisitionService.beginAcquisite(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(BeginAcquisiteRs.ext, resp);
		}
	}

}
