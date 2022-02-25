package com.gryphpoem.game.zw.handler.client.acquisition;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.UseFreeSpeedRq;
import com.gryphpoem.game.zw.pb.GamePb4.UseFreeSpeedRs;
import com.gryphpoem.game.zw.service.AcquisitionService;

/**
 * 
 * @Description 使用免费加速
 * @author TanDonghai
 *
 */
public class UseFreeSpeedHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		UseFreeSpeedRq req = msg.getExtension(UseFreeSpeedRq.ext);
		AcquisitionService acquisitionService = getService(AcquisitionService.class);
		UseFreeSpeedRs resp = acquisitionService.useFreeSpeed(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(UseFreeSpeedRs.ext, resp);
		}
	}

}
