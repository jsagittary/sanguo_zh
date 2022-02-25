package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.JoinBattleRq;
import com.gryphpoem.game.zw.pb.GamePb2.JoinBattleRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * 
 * @Description 加入城战（攻击玩家）或国战
 * @author TanDonghai
 *
 */
public class JoinBattleHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		JoinBattleRq req = msg.getExtension(JoinBattleRq.ext);
		WorldService worldService = getService(WorldService.class);
		JoinBattleRs resp = worldService.joinBattle(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(JoinBattleRs.ext, resp);
		}
	}

}
