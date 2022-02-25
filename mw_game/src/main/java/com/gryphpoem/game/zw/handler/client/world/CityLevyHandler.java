package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.CityLevyRq;
import com.gryphpoem.game.zw.pb.GamePb2.CityLevyRs;
import com.gryphpoem.game.zw.service.CityService;

/**
 * 
 * @Description 城池征收
 * @author TanDonghai
 *
 */
public class CityLevyHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		CityLevyRq req = msg.getExtension(CityLevyRq.ext);
		CityService cityService = getService(CityService.class);
		CityLevyRs resp = cityService.cityLevy(getRoleId(), req.getCityId());

		if (null != resp) {
			sendMsgToPlayer(CityLevyRs.ext, resp);
		}
	}

}
