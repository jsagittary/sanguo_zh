package com.gryphpoem.game.zw.handler.client.fish;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.fish.FishingService;

/**
 * @author xwind
 * @date 2021/8/12
 */
public class FishingFisheryThrowRodHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GamePb4.FishingFisheryThrowRodRq req = msg.getExtension(GamePb4.FishingFisheryThrowRodRq.ext);
		GamePb4.FishingFisheryThrowRodRs resp = getService(FishingService.class).fisheryThrowRod(getRoleId(), req);
		sendMsgToPlayer(GamePb4.FishingFisheryThrowRodRs.EXT_FIELD_NUMBER, GamePb4.FishingFisheryThrowRodRs.ext, resp);
	}
}
