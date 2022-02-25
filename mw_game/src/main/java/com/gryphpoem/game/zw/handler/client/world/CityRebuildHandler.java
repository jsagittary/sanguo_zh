package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.CityRebuildRq;
import com.gryphpoem.game.zw.pb.GamePb2.CityRebuildRs;
import com.gryphpoem.game.zw.service.CityService;

/**
 * 
 * @Description 城池重建
 * @author TanDonghai
 *
 */
public class CityRebuildHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		CityRebuildRq req = msg.getExtension(CityRebuildRq.ext);
		CityService cityService = getService(CityService.class);
		CityRebuildRs resp = cityService.cityRebuild(getRoleId(), req.getCityId());

		if (null != resp) {
			sendMsgToPlayer(CityRebuildRs.ext, resp);
		}
	}

}
