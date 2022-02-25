package com.gryphpoem.game.zw.handler.client.common;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetTimeRs;
import com.gryphpoem.game.zw.service.PlayerService;

/**
 * 
 * @Description 获取服务器时间
 * @author TanDonghai
 *
 */
public class GetTimeHandler extends ClientHandler {

	@Override
	public void action() {
		PlayerService playerService = getService(PlayerService.class);
		GetTimeRs resp = playerService.getTime(this);

		if (null != resp) {
			sendMsgToPlayer(GetTimeRs.ext, resp);
		}
	}

}
