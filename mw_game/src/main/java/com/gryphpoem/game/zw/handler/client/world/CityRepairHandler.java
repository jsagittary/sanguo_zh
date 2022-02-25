package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.CityRepairRq;
import com.gryphpoem.game.zw.pb.GamePb2.CityRepairRs;
import com.gryphpoem.game.zw.service.CityService;

/**
 * 
 * @Description 城池修复
 * @author TanDonghai
 *
 */
public class CityRepairHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		CityRepairRq req = msg.getExtension(CityRepairRq.ext);
		CityService cityService = getService(CityService.class);
		CityRepairRs resp = cityService.cityRepair(getRoleId(), req.getCityId());

		if (null != resp) {
			sendMsgToPlayer(CityRepairRs.ext, resp);
		}
	}

}
