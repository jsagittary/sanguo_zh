package com.gryphpoem.game.zw.handler.client.treasure;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetTreasureRq;
import com.gryphpoem.game.zw.pb.GamePb1.GetTreasureRs;
import com.gryphpoem.game.zw.service.TreasureService;

/**
 * 
 * @Description 聚宝盆
 * @author tyler
 *
 */
public class GetTreasureHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetTreasureRq req = msg.getExtension(GetTreasureRq.ext);
		TreasureService equipService = getService(TreasureService.class);
		GetTreasureRs resp = equipService.getTreasure(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(GetTreasureRs.ext, resp);
		}
	}

}
