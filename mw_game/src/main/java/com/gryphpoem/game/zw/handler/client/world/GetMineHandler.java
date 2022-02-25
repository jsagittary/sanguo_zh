package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetMineRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetMineRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * 
 * @Description 获取矿点采集详情
 * @author TanDonghai
 *
 */
public class GetMineHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetMineRq req = msg.getExtension(GetMineRq.ext);
		WorldService worldService = getService(WorldService.class);
		GetMineRs resp = worldService.getMine(getRoleId(), req.getPos());

		if (null != resp) {
			sendMsgToPlayer(GetMineRs.ext, resp);
		}
	}

}
