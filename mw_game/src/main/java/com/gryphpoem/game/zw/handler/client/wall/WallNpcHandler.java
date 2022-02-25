package com.gryphpoem.game.zw.handler.client.wall;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.WallNpcRq;
import com.gryphpoem.game.zw.pb.GamePb1.WallNpcRs;
import com.gryphpoem.game.zw.service.WallService;

/**
 * 城墙招募NPC
 * 
 * @author tyler
 *
 */
public class WallNpcHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		WallNpcRq req = msg.getExtension(WallNpcRq.ext);
		WallNpcRs resp = getService(WallService.class).doWallNpc(getRoleId(), req.getPos());
		sendMsgToPlayer(WallNpcRs.EXT_FIELD_NUMBER, WallNpcRs.ext, resp);
	}
}
