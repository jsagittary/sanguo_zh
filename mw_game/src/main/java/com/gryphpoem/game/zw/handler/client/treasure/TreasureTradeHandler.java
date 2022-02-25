package com.gryphpoem.game.zw.handler.client.treasure;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.TreasureTradeRq;
import com.gryphpoem.game.zw.pb.GamePb1.TreasureTradeRs;
import com.gryphpoem.game.zw.service.TreasureService;

/**
 * 
 * @Description 聚宝盆兑换
 * @author tyler
 *
 */
public class TreasureTradeHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		TreasureTradeRq req = msg.getExtension(TreasureTradeRq.ext);
		TreasureService service = getService(TreasureService.class);
		TreasureTradeRs resp = service.treasureTrade(getRoleId(), req.getCostId(), req.getGainId());

		if (null != resp) {
			sendMsgToPlayer(TreasureTradeRs.ext, resp);
		}
	}

}
