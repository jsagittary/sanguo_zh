package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.UpCityRq;
import com.gryphpoem.game.zw.pb.GamePb2.UpCityRs;
import com.gryphpoem.game.zw.service.CityService;

/**
 * 都城升级
 * @author tyler
 *
 */
public class UpCityHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		UpCityRq req = msg.getExtension(UpCityRq.ext);
		CityService service = getService(CityService.class);
		UpCityRs resp = service.upCity(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(UpCityRs.ext, resp);
		}
	}

}
