package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetHerosRs;
import com.gryphpoem.game.zw.service.HeroService;

/**
 * 
 * @Description 获取所有将领
 * @author TanDonghai
 *
 */
public class GetHerosHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		HeroService heroService = getService(HeroService.class);
		GetHerosRs resp = heroService.getHeros(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(GetHerosRs.ext, resp);
		}
	}

}
