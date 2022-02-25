package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetMapRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetMapRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * 
 * @Description 获取地图中某个区块的数据
 * @author TanDonghai
 *
 */
public class GetMapHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetMapRq req = msg.getExtension(GetMapRq.ext);
		WorldService worldService = getService(WorldService.class);
		GetMapRs resp = worldService.getMap(getRoleId(), req.getBlockList());

		if (null != resp) {
			sendMsgToPlayer(GetMapRs.ext, resp);
		}
	}

}
