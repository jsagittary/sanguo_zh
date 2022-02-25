package com.gryphpoem.game.zw.handler.client.fish;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.fish.FishingService;

/**
 * @author xwind
 * @date 2021/8/12
 */
public class FishingShopGetInfoHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GamePb4.FishingShopGetInfoRs resp = getService(FishingService.class).shopGetInfo(getRoleId());
		sendMsgToPlayer(GamePb4.FishingShopGetInfoRs.EXT_FIELD_NUMBER, GamePb4.FishingShopGetInfoRs.ext, resp);
	}
}
