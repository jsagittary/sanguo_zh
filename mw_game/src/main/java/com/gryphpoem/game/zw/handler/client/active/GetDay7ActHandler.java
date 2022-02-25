package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetDay7ActRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

public class GetDay7ActHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetDay7ActRs resp = getService(ActivityService.class).getDay7ActRs(getRoleId());
		sendMsgToPlayer(GetDay7ActRs.EXT_FIELD_NUMBER, GetDay7ActRs.ext, resp);
	}

}
