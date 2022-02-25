package com.gryphpoem.game.zw.handler.client.wall;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.WallHelpInfoRq;
import com.gryphpoem.game.zw.pb.GamePb1.WallHelpInfoRs;
import com.gryphpoem.game.zw.service.WallService;

/**
 * 城墙驻防信息
 * 
 * @author tyler
 *
 */
public class WallHelpInfoHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		WallHelpInfoRq req = msg.getExtension(WallHelpInfoRq.ext);
		WallHelpInfoRs resp = getService(WallService.class).doWallHelpInfo(getRoleId(), req.getPos());
		sendMsgToPlayer(WallHelpInfoRs.EXT_FIELD_NUMBER, WallHelpInfoRs.ext, resp);
	}
}
