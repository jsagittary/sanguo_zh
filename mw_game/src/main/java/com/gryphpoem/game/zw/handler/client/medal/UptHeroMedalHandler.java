package com.gryphpoem.game.zw.handler.client.medal;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.UptHeroMedalRq;
import com.gryphpoem.game.zw.pb.GamePb1.UptHeroMedalRs;
import com.gryphpoem.game.zw.service.MedalService;
/**
 * 
* @ClassName: UptHeroMedalHandler
* @Description: 将领穿戴或更换勋章
* @author chenqi
* @date 2018年9月13日
*
 */
public class UptHeroMedalHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		UptHeroMedalRq req = msg.getExtension(UptHeroMedalRq.ext);
		MedalService medalService = getService(MedalService.class);
		UptHeroMedalRs resp = medalService.uptHeroMedal(getRoleId(),req.getHeroId(),req.getKeyId(),req.getType());

		if (null != resp) {
			sendMsgToPlayer(UptHeroMedalRs.ext, resp);
		}
	}

}
