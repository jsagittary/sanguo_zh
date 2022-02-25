package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetMarchRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetMarchRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * 
 * @Description 获取区域内的行军路线
 * @author TanDonghai
 *
 */
public class GetMarchHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetMarchRq req = msg.getExtension(GetMarchRq.ext);
		WorldService worldService = getService(WorldService.class);
		GetMarchRs resp = worldService.getMarch(getRoleId(), req.getArea());

		if (null != resp) {
			sendMsgToPlayer(GetMarchRs.ext, resp);
		}
	}

}
