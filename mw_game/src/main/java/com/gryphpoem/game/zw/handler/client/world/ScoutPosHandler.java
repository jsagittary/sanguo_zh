package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.ScoutPosRq;
import com.gryphpoem.game.zw.pb.GamePb2.ScoutPosRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * 
 * @Description 侦查
 * @author TanDonghai
 *
 */
public class ScoutPosHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		ScoutPosRq req = msg.getExtension(ScoutPosRq.ext);
		WorldService worldService = getService(WorldService.class);
		ScoutPosRs resp = worldService.scoutPos(getRoleId(), req.getPos(), req.getType());

		if (null != resp) {
			sendMsgToPlayer(ScoutPosRs.ext, resp);
		}
	}

}
