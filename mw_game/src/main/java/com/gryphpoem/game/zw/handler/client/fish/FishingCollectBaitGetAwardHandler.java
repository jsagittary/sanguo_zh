package com.gryphpoem.game.zw.handler.client.fish;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.fish.FishingService;

/**
 * @author xwind
 * @date 2021/8/12
 */
public class FishingCollectBaitGetAwardHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GamePb4.FishingCollectBaitGetAwardRq req = msg.getExtension(GamePb4.FishingCollectBaitGetAwardRq.ext);
		GamePb4.FishingCollectBaitGetAwardRs resp = getService(FishingService.class).collectBaitGetAward(getRoleId(), req);
		sendMsgToPlayer(GamePb4.FishingCollectBaitGetAwardRs.EXT_FIELD_NUMBER, GamePb4.FishingCollectBaitGetAwardRs.ext, resp);
	}
}
