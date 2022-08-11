package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.HeroWashRq;
import com.gryphpoem.game.zw.pb.GamePb1.HeroWashRs;
import com.gryphpoem.game.zw.service.HeroService;

/**
 * 
 * @Description 将领洗髓
 * @author TanDonghai
 *
 */
public class HeroWashHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
//		HeroWashRq req = msg.getExtension(HeroWashRq.ext);
//		HeroService heroService = getService(HeroService.class);
//		HeroWashRs resp = heroService.heroWash(getRoleId(), req);
//
//		if (null != resp) {
//			sendMsgToPlayer(HeroWashRs.ext, resp);
//		}
	}

}
