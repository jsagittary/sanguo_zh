package com.gryphpoem.game.zw.handler.client.fish;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.fish.FishingService;

/**
 * @author xwind
 * @date 2021/8/12
 */
public class FishingShopExchangeHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GamePb4.FishingShopExchangeRq req = msg.getExtension(GamePb4.FishingShopExchangeRq.ext);
		GamePb4.FishingShopExchangeRs resp = getService(FishingService.class).shopExchange(getRoleId(), req.getId());
		sendMsgToPlayer(GamePb4.FishingShopExchangeRs.EXT_FIELD_NUMBER, GamePb4.FishingShopExchangeRs.ext, resp);
	}
}
