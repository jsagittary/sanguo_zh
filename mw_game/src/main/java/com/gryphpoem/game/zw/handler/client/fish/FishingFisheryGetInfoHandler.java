package com.gryphpoem.game.zw.handler.client.fish;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.fish.FishingService;

/**
 * @author xwind
 * @date 2021/8/12
 */
public class FishingFisheryGetInfoHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GamePb4.FishingFisheryGetInfoRs resp = getService(FishingService.class).fisheryGetInfo(getRoleId());
		sendMsgToPlayer(GamePb4.FishingFisheryGetInfoRs.EXT_FIELD_NUMBER, GamePb4.FishingFisheryGetInfoRs.ext, resp);
	}
}
