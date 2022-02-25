package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.HeroPosSetRq;
import com.gryphpoem.game.zw.pb.GamePb1.HeroPosSetRs;
import com.gryphpoem.game.zw.service.HeroService;

/**
 * 将领换位置
 * 
 * @author tyler
 *
 */
public class HeroPosSetHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		HeroPosSetRq req = msg.getExtension(HeroPosSetRq.ext);
		HeroService heroService = getService(HeroService.class);
		HeroPosSetRs resp = heroService.heroPosSet(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(HeroPosSetRs.ext, resp);
		}
	}

}
