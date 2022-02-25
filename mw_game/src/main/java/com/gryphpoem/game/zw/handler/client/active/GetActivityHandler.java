package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetActivityRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetActivityRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * 获取某个活动列表
 * @author tyler
 *
 */
public class GetActivityHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetActivityRq req = msg.getExtension(GetActivityRq.ext);
		GetActivityRs resp = getService(ActivityService.class).getActivity(getRoleId(), req.getType());
		sendMsgToPlayer(GetActivityRs.EXT_FIELD_NUMBER, GetActivityRs.ext, resp);
	}

}
