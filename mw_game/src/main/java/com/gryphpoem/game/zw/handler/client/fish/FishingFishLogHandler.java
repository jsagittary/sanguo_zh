package com.gryphpoem.game.zw.handler.client.fish;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.fish.FishingService;

/**
 * @author xwind
 * @date 2021/8/12
 */
public class FishingFishLogHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GamePb4.FishingFishLogRs resp = getService(FishingService.class).fishLogs(getRoleId());
		sendMsgToPlayer(GamePb4.FishingFishLogRs.EXT_FIELD_NUMBER, GamePb4.FishingFishLogRs.ext, resp);
	}
}
