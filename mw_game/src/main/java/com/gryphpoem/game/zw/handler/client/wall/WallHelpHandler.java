package com.gryphpoem.game.zw.handler.client.wall;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.WallHelpRq;
import com.gryphpoem.game.zw.pb.GamePb1.WallHelpRs;
import com.gryphpoem.game.zw.service.WallService;

/**
 * 城墙驻防他人
 * 
 * @author tyler
 *
 */
public class WallHelpHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		WallHelpRq req = msg.getExtension(WallHelpRq.ext);
		WallHelpRs resp = getService(WallService.class).doWallHelp(getRoleId(), req.getPos(), req.getHeroIdList());
		sendMsgToPlayer(WallHelpRs.EXT_FIELD_NUMBER, WallHelpRs.ext, resp);
	}
}
