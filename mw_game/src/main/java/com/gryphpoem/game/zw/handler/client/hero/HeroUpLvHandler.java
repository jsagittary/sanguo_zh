package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.HeroQuickUpLvRs;
import com.gryphpoem.game.zw.pb.GamePb4.HeroQuickUpLvRq;
import com.gryphpoem.game.zw.service.HeroService;

/**
 * 
 * @Description 将领直接升一级
 * @author shi.pei
 *
 */
public class HeroUpLvHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		HeroQuickUpLvRq req = msg.getExtension(HeroQuickUpLvRq.ext);
		HeroService heroService = getService(HeroService.class);
		HeroQuickUpLvRs resp = heroService.heroQuickUpLv(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(HeroQuickUpLvRs.ext, resp);
		}
	}

}
