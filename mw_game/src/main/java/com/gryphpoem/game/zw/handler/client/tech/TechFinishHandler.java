package com.gryphpoem.game.zw.handler.client.tech;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.TechFinishRs;
import com.gryphpoem.game.zw.service.TechService;

/**
 * 科技升级完成
 * 
 * @author tyler
 *
 */
public class TechFinishHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		TechService buildingService = getService(TechService.class);
		TechFinishRs resp = buildingService.doTechFinish(getRoleId());
		sendMsgToPlayer(TechFinishRs.EXT_FIELD_NUMBER, TechFinishRs.ext, resp);
	}
}
