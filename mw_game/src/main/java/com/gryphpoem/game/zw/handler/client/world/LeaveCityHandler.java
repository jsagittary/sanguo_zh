package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.LeaveCityRq;
import com.gryphpoem.game.zw.pb.GamePb2.LeaveCityRs;
import com.gryphpoem.game.zw.service.CityService;

/**
 * 
 * @Description 城主撤离城池
 * @author TanDonghai
 *
 */
public class LeaveCityHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		LeaveCityRq req = msg.getExtension(LeaveCityRq.ext);
		CityService cityService = getService(CityService.class);
		LeaveCityRs resp = cityService.leaveCity(getRoleId(), req.getCityId());

		if (null != resp) {
			sendMsgToPlayer(LeaveCityRs.ext, resp);
		}
	}

}
