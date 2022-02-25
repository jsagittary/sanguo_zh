package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.MoveCityRq;
import com.gryphpoem.game.zw.pb.GamePb2.MoveCityRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * 
 * @Description 玩家迁城
 * @author TanDonghai
 *
 */
public class MoveCityHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		MoveCityRq req = msg.getExtension(MoveCityRq.ext);
		WorldService worldService = getService(WorldService.class);
		MoveCityRs resp = worldService.moveCity(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(MoveCityRs.ext, resp);
		}
	}

}
