package com.gryphpoem.game.zw.handler.client.wall;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.WallSetRq;
import com.gryphpoem.game.zw.pb.GamePb1.WallSetRs;
import com.gryphpoem.game.zw.service.WallService;

/**
 * 城墙布置
 * 
 * @author tyler
 *
 */
public class WallSetHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		WallSetRq req = msg.getExtension(WallSetRq.ext);
		boolean swap = req.hasSwap() && req.getSwap();
		// boolean swapTreasure = req.hasSwapTreasure() && req.getSwapTreasure();
		// boolean swapMedal = req.hasSwapMedal() && req.getSwapMedal();
		WallSetRs resp = getService(WallService.class).doWallSet(getRoleId(), req.getPos(), req.getHeroId(),
				req.getType(), swap/*, swapTreasure, swapMedal*/);
		sendMsgToPlayer(WallSetRs.EXT_FIELD_NUMBER, WallSetRs.ext, resp);
	}
}
