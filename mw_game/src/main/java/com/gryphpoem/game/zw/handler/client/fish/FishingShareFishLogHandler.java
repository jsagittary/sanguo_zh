package com.gryphpoem.game.zw.handler.client.fish;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.fish.FishingService;

/**
 * @author xwind
 * @date 2021/8/12
 */
public class FishingShareFishLogHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GamePb4.FishingShareFishLogRq req = msg.getExtension(GamePb4.FishingShareFishLogRq.ext);
		GamePb4.FishingShareFishLogRs resp = getService(FishingService.class).shareFishLog(getRoleId(), req.getLogId());
		sendMsgToPlayer(GamePb4.FishingShareFishLogRs.EXT_FIELD_NUMBER, GamePb4.FishingShareFishLogRs.ext, resp);
	}
}
