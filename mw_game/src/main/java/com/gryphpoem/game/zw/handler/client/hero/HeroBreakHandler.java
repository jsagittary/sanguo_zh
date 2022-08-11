package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.HeroBreakRq;
import com.gryphpoem.game.zw.pb.GamePb1.HeroBreakRs;
import com.gryphpoem.game.zw.service.HeroService;

/**
 * 
 * @Description 将领突破
 * @author TanDonghai
 *
 */
public class HeroBreakHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
//		HeroBreakRq req = msg.getExtension(HeroBreakRq.ext);
//		HeroService heroService = getService(HeroService.class);
//		HeroBreakRs resp = heroService.heroBreak(getRoleId(), req);
//
//		if (null != resp) {
//			sendMsgToPlayer(HeroBreakRs.ext, resp);
//		}
	}

}
