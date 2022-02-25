package com.gryphpoem.game.zw.handler.client.wall;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.WallNpcAutoRq;
import com.gryphpoem.game.zw.pb.GamePb1.WallNpcAutoRs;
import com.gryphpoem.game.zw.service.WallService;

/**
 * 城墙NPC开启自动补兵
 * 
 * @author tyler
 *
 */
public class WallNpcAutoHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		WallNpcAutoRq req = msg.getExtension(WallNpcAutoRq.ext);
		WallNpcAutoRs resp = getService(WallService.class).doWallNpcAuto(getRoleId(), req.getId());
		sendMsgToPlayer(WallNpcAutoRs.EXT_FIELD_NUMBER, WallNpcAutoRs.ext, resp);
	}
}
