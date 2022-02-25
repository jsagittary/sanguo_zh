package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.PlayLuckyPoolRq;
import com.gryphpoem.game.zw.pb.GamePb4.PlayLuckyPoolRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

public class PlayLuckyPoolHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		PlayLuckyPoolRq req = msg.getExtension(PlayLuckyPoolRq.ext);
		ActivityService service = getService(ActivityService.class);
		PlayLuckyPoolRs resp = service.playLuckyPool(getRoleId(), req.getTurnplateId());
		if (null != resp) {
			sendMsgToPlayer(PlayLuckyPoolRs.ext, resp);
		}
	}

}
