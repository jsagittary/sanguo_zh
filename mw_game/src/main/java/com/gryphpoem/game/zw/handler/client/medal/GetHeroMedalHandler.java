package com.gryphpoem.game.zw.handler.client.medal;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetHeroMedalRq;
import com.gryphpoem.game.zw.pb.GamePb1.GetHeroMedalRs;
import com.gryphpoem.game.zw.service.MedalService;
/**
 * 
* @ClassName: GetHeroMedalHandler
* @Description: 获取指定将领的勋章
* @author chenqi
* @date 2018年9月12日
*
 */
public class GetHeroMedalHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetHeroMedalRq req = msg.getExtension(GetHeroMedalRq.ext);
		MedalService medalService = getService(MedalService.class);
		GetHeroMedalRs resp = medalService.getHeroMedal(getRoleId(),req.getHeroId());

		if (null != resp) {
			sendMsgToPlayer(GetHeroMedalRs.ext, resp);
		}
	}
}
