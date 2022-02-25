package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetCampBattleRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * 
 * @Description 获取阵营战信息
 * @author TanDonghai
 *
 */
public class GetCampBattleHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		WorldService worldService = getService(WorldService.class);
		GetCampBattleRs resp = worldService.getCampBattle(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(GetCampBattleRs.ext, resp);
		}
	}

}
