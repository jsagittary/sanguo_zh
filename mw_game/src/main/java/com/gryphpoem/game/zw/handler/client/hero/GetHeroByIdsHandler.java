package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetHeroByIdsRq;
import com.gryphpoem.game.zw.pb.GamePb1.GetHeroByIdsRs;
import com.gryphpoem.game.zw.service.HeroService;

/**
 * 
 * @Description 获取所有将领
 * @author TanDonghai
 *
 */
public class GetHeroByIdsHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
	    GetHeroByIdsRq req = msg.getExtension(GetHeroByIdsRq.ext);
		HeroService heroService = getService(HeroService.class);
		GetHeroByIdsRs resp = heroService.getHeroByIds(getRoleId(), req.getHeroIdsList());

		if (null != resp) {
			sendMsgToPlayer(GetHeroByIdsRs.ext, resp);
		}
	}

}
