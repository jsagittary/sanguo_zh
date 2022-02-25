package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.ClearCDRq;
import com.gryphpoem.game.zw.pb.GamePb2.ClearCDRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * 
 * @Description 清除CD时间
 * @author TanDonghai
 *
 */
public class ClearCDHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		ClearCDRq req = msg.getExtension(ClearCDRq.ext);
		WorldService worldService = getService(WorldService.class);
		ClearCDRs resp = worldService.clearCD(getRoleId(), req.getType());

		if (null != resp) {
			sendMsgToPlayer(ClearCDRs.ext, resp);
		}
	}

}
