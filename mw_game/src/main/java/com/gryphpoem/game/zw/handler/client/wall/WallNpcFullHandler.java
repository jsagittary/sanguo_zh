package com.gryphpoem.game.zw.handler.client.wall;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.WallNpcFullRq;
import com.gryphpoem.game.zw.pb.GamePb1.WallNpcFullRs;
import com.gryphpoem.game.zw.service.WallService;

/**
 * 城墙花钱满兵
 * 
 * @author tyler
 *
 */
public class WallNpcFullHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		WallNpcFullRq req = msg.getExtension(WallNpcFullRq.ext);
		WallNpcFullRs resp = getService(WallService.class).doWallNpcFull(getRoleId(), req.getType(), req.getId());
		sendMsgToPlayer(WallNpcFullRs.EXT_FIELD_NUMBER, WallNpcFullRs.ext, resp);
	}
}
