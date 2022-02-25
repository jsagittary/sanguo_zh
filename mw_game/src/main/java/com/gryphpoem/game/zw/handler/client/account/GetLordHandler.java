package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetLordRs;
import com.gryphpoem.game.zw.service.PlayerService;

/**
 * 
 * @Description 获取玩家数据
 * @author TanDonghai
 *
 */
public class GetLordHandler extends ClientHandler {

	@Override
	public void action() {
		PlayerService playerService = getService(PlayerService.class);
		GetLordRs resp = playerService.getLord(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(GetLordRs.ext, resp);
		}
	}
}
