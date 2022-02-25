package com.gryphpoem.game.zw.handler.client.wall;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.WallCallBackRq;
import com.gryphpoem.game.zw.pb.GamePb1.WallCallBackRs;
import com.gryphpoem.game.zw.service.WallService;

/**
 * 城墙驻防召回
 * 
 * @author tyler
 *
 */
public class WallCallBackHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		WallCallBackRq req = msg.getExtension(WallCallBackRq.ext);
		WallCallBackRs resp = getService(WallService.class).doWallCallBack(getRoleId(), req.getKeyId());
		sendMsgToPlayer(WallCallBackRs.EXT_FIELD_NUMBER, WallCallBackRs.ext, resp);
	}
}
