package com.gryphpoem.game.zw.handler.client.acquisition;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetAcquisitionRs;
import com.gryphpoem.game.zw.service.AcquisitionService;

/**
 * 
 * @Description 获取玩家个人资源点数据
 * @author TanDonghai
 *
 */
public class GetAcquisitionHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		AcquisitionService acquisitionService = getService(AcquisitionService.class);
		GetAcquisitionRs resp = acquisitionService.getAcquisition(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(GetAcquisitionRs.ext, resp);
		}
	}

}
