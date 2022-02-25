package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.SaveHeroWashRq;
import com.gryphpoem.game.zw.pb.GamePb1.SaveHeroWashRs;
import com.gryphpoem.game.zw.service.HeroService;

/**
 * 
 * @Description 保存将领洗髓
 * @author TanDonghai
 *
 */
public class SaveHeroWashHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		SaveHeroWashRq req = msg.getExtension(SaveHeroWashRq.ext);
		HeroService heroService = getService(HeroService.class);
		SaveHeroWashRs resp = heroService.saveHeroWash(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(SaveHeroWashRs.ext, resp);
		}
	}

}
