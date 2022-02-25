package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.GamePb3.GetActivityAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetActivityAwardRs;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * 活动领奖
 * @author tyler
 *
 */
public class GetActivityAwardHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetActivityAwardRq req = msg.getExtension(GetActivityAwardRq.ext);

		// 如果是分享好友奖励
		if (req.getActivityType() == ActivityConst.ACT_SHARE_REWARD) {
			sendMsgToPlayer(GetActivityAwardRs.ext, GetActivityAwardRs.newBuilder().build());
			BasePb.Base.Builder message = getService(ActivityService.class).shareRewardProcess(req, getRoleId());
			// 发送请求到账号服请求数据
			sendMsgToPublic(message);
		} else {
			// 其它的活动
			GetActivityAwardRs resp = getService(ActivityService.class).getActivityAward(req, getRoleId());
			sendMsgToPlayer(GetActivityAwardRs.EXT_FIELD_NUMBER, GetActivityAwardRs.ext, resp);
		}

	}

}
