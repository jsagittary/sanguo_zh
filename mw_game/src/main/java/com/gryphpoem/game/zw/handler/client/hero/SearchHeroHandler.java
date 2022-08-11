package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.SearchHeroRq;
import com.gryphpoem.game.zw.pb.GamePb1.SearchHeroRs;
import com.gryphpoem.game.zw.service.HeroService;

/**
 * 
 * @Description 良将寻访
 * @author TanDonghai
 *
 */
public class SearchHeroHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
//		SearchHeroRq req = msg.getExtension(SearchHeroRq.ext);
//		HeroService heroService = getService(HeroService.class);
//		SearchHeroRs resp = heroService.searchHero(getRoleId(), req);
//
//		if (null != resp) {
//			sendMsgToPlayer(SearchHeroRs.ext, resp);
//		}
	}

}
