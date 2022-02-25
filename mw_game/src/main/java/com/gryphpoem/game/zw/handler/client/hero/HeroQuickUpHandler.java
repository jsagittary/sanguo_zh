package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.HeroQuickUpRq;
import com.gryphpoem.game.zw.pb.GamePb1.HeroQuickUpRs;
import com.gryphpoem.game.zw.service.HeroService;

/**
 * 
 * @Description 将领快速升级
 * @author TanDonghai
 *
 */
public class HeroQuickUpHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		HeroQuickUpRq req = msg.getExtension(HeroQuickUpRq.ext);
		HeroService heroService = getService(HeroService.class);
		HeroQuickUpRs resp = heroService.heroQuickUp(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(HeroQuickUpRs.ext, resp);
		}
	}

}
