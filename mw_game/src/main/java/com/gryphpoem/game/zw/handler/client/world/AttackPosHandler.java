package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.AttackPosRq;
import com.gryphpoem.game.zw.pb.GamePb2.AttackPosRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * 
 * @Description 攻击某个坐标的势力（包括玩家和流寇）
 * @author TanDonghai
 *
 */
public class AttackPosHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		AttackPosRq req = msg.getExtension(AttackPosRq.ext);
		WorldService worldService = getService(WorldService.class);
		AttackPosRs resp = worldService.attackPos(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(AttackPosRs.ext, resp);
		}
	}

}
