package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetLuckyPoolRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

public class GetLuckyPoolHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		ActivityService service = getService(ActivityService.class);
		GetLuckyPoolRs resp = service.getLuckyPool(getRoleId());
		if (null != resp) {
			sendMsgToPlayer(GetLuckyPoolRs.ext, resp);
		}
	}

}
