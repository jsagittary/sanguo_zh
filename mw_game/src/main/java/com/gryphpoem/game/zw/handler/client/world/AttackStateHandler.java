package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.AttackStateRq;
import com.gryphpoem.game.zw.pb.GamePb2.AttackStateRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * 
 * @Description 发起国战（攻击其他国家城池）
 * @author TanDonghai
 *
 */
public class AttackStateHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		AttackStateRq req = msg.getExtension(AttackStateRq.ext);
		WorldService worldService = getService(WorldService.class);
		AttackStateRs resp = worldService.attackState(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(AttackStateRs.ext, resp);
		}
	}

}
