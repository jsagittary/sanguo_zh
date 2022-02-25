package com.gryphpoem.game.zw.handler.client.wall;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.WallNpcArmyRq;
import com.gryphpoem.game.zw.pb.GamePb1.WallNpcArmyRs;
import com.gryphpoem.game.zw.service.WallService;

/**
 * 城墙NPC变兵种
 * 
 * @author tyler
 *
 */
public class WallNpcArmyHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		WallNpcArmyRq req = msg.getExtension(WallNpcArmyRq.ext);
		WallNpcArmyRs resp = getService(WallService.class).doWallNpcArmy(getRoleId(), req.getId());
		sendMsgToPlayer(WallNpcArmyRs.EXT_FIELD_NUMBER, WallNpcArmyRs.ext, resp);
	}
}
