package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.SetGuideRq;
import com.gryphpoem.game.zw.pb.GamePb1.SetGuideRs;
import com.gryphpoem.game.zw.service.PlayerService;

/**
 * 
 * @Description 设置玩家当前新手引导进度
 * @author TanDonghai
 *
 */
public class SetGuideHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		SetGuideRq req = msg.getExtension(SetGuideRq.ext);
		PlayerService playerService = getService(PlayerService.class);

		SetGuideRs resp = playerService.setGuide(req, getRoleId());
		sendMsgToPlayer(SetGuideRs.ext, resp);
	}

}
