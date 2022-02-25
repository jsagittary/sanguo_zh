package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetActivityListRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

public class GetActivityListHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetActivityListRs resp = getService(ActivityService.class).getActivityList(getRoleId());
		sendMsgToPlayer(GetActivityListRs.EXT_FIELD_NUMBER, GetActivityListRs.ext, resp);
	}

}
