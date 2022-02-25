package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetAreaRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetAreaRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * 
 * @Description 获取某个行政区域的数据
 * @author TanDonghai
 *
 */
public class GetAreaHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetAreaRq req = msg.getExtension(GetAreaRq.ext);
		WorldService worldService = getService(WorldService.class);
		GetAreaRs resp = worldService.getArea(getRoleId(), req.getArea());

		if (null != resp) {
			sendMsgToPlayer(GetAreaRs.ext, resp);
		}
	}

}
