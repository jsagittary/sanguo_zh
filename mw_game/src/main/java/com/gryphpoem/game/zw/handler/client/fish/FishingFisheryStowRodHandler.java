package com.gryphpoem.game.zw.handler.client.fish;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.fish.FishingService;

/**
 * @author xwind
 * @date 2021/8/12
 */
public class FishingFisheryStowRodHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GamePb4.FishingFisheryStowRodRq req = msg.getExtension(GamePb4.FishingFisheryStowRodRq.ext);
		GamePb4.FishingFisheryStowRodRs resp = getService(FishingService.class).fisheryStowRod(getRoleId(), req);
		sendMsgToPlayer(GamePb4.FishingFisheryStowRodRs.EXT_FIELD_NUMBER, GamePb4.FishingFisheryStowRodRs.ext, resp);
	}
}
