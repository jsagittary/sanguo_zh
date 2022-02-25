package com.gryphpoem.game.zw.handler.client.fish;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.fish.FishingService;

/**
 * @author xwind
 * @date 2021/8/12
 */
public class FishingAltasHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GamePb4.FishingAltasRq req = msg.getExtension(GamePb4.FishingAltasRq.ext);
		GamePb4.FishingAltasRs resp = getService(FishingService.class).getAltas(getRoleId(), req.getType());
		sendMsgToPlayer(GamePb4.FishingAltasRs.EXT_FIELD_NUMBER, GamePb4.FishingAltasRs.ext, resp);
	}
}
