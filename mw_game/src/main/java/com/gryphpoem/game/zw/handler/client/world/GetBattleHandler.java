package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetBattleRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetBattleRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * 
 * @Description 获取城战或国战详情
 * @author TanDonghai
 *
 */
public class GetBattleHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetBattleRq req = msg.getExtension(GetBattleRq.ext);
		WorldService worldService = getService(WorldService.class);
		GetBattleRs resp = worldService.getBattle(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(GetBattleRs.ext, resp);
		}
	}

}
