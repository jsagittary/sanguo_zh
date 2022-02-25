package com.gryphpoem.game.zw.handler.client.fish;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.fish.FishingService;

/**
 * @author xwind
 * @date 2021/8/12
 */
public class FishingCollectBaitGetInfoHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GamePb4.FishingCollectBaitGetInfoRq req = msg.getExtension(GamePb4.FishingCollectBaitGetInfoRq.ext);
		GamePb4.FishingCollectBaitGetInfoRs resp = getService(FishingService.class).collectBaitGetInfo(getRoleId());
		sendMsgToPlayer(GamePb4.FishingCollectBaitGetInfoRs.EXT_FIELD_NUMBER, GamePb4.FishingCollectBaitGetInfoRs.ext, resp);
	}
}
