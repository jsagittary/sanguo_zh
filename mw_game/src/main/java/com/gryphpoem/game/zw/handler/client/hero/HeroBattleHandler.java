package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.HeroBattleRq;
import com.gryphpoem.game.zw.pb.GamePb1.HeroBattleRs;
import com.gryphpoem.game.zw.service.HeroService;

/**
 * 
 * @Description 将领上阵
 * @author TanDonghai
 *
 */
public class HeroBattleHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		HeroBattleRq req = msg.getExtension(HeroBattleRq.ext);
		HeroService heroService = getService(HeroService.class);
		HeroBattleRs resp = heroService.heroBattle(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(HeroBattleRs.ext, resp);
		}
	}

}
