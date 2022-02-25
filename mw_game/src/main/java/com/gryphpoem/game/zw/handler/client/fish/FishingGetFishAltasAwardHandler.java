package com.gryphpoem.game.zw.handler.client.fish;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.fish.FishingService;

/**
 * @author xwind
 * @date 2021/8/12
 */
public class FishingGetFishAltasAwardHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GamePb4.FishingGetFishAltasAwardRq req = msg.getExtension(GamePb4.FishingGetFishAltasAwardRq.ext);
		GamePb4.FishingGetFishAltasAwardRs resp = getService(FishingService.class).getFishAltasAward(getRoleId(), req.getFishId());
		sendMsgToPlayer(GamePb4.FishingGetFishAltasAwardRs.EXT_FIELD_NUMBER, GamePb4.FishingGetFishAltasAwardRs.ext, resp);
	}
}
