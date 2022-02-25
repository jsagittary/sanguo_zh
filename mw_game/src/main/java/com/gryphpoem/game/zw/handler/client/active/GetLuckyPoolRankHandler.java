package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetLuckyPoolRankRs;
import com.gryphpoem.game.zw.pb.GamePb4.GetLuckyPoolRankRq;
import com.gryphpoem.game.zw.service.activity.ActivityService;

public class GetLuckyPoolRankHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetLuckyPoolRankRq req = msg.getExtension(GetLuckyPoolRankRq.ext);
		ActivityService service = getService(ActivityService.class);
		GetLuckyPoolRankRs resp = service.getLuckyPoolRank(req.getDate());
		if (null != resp) {
			sendMsgToPlayer(GetLuckyPoolRankRs.ext, resp);
		}
	}

}
