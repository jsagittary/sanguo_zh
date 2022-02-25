package com.gryphpoem.game.zw.handler.client.wall;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.WallNpcLvUpRq;
import com.gryphpoem.game.zw.pb.GamePb1.WallNpcLvUpRs;
import com.gryphpoem.game.zw.service.WallService;

/**
 * 城墙NPC升级
 * 
 * @author tyler
 *
 */
public class WallNpcLvUpHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		WallNpcLvUpRq req = msg.getExtension(WallNpcLvUpRq.ext);
		WallNpcLvUpRs resp = getService(WallService.class).doWallNpcLvUp(getRoleId(), req.getPos(), req.getType());
		sendMsgToPlayer(WallNpcLvUpRs.EXT_FIELD_NUMBER, WallNpcLvUpRs.ext, resp);
	}
}
