package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.RetreatRq;
import com.gryphpoem.game.zw.pb.GamePb2.RetreatRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * 
 * @Description 撤回部队
 * @author TanDonghai
 *
 */
public class RetreatHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		RetreatRq req = msg.getExtension(RetreatRq.ext);
		WorldService worldService = getService(WorldService.class);
		RetreatRs resp = worldService.retreat(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(RetreatRs.ext, resp);
		}
	}

}
