package com.gryphpoem.game.zw.handler.client.prop;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetPropsRs;
import com.gryphpoem.game.zw.service.PropService;

/**
 * 
 * @Description 获取玩家背包的道具
 * @author TanDonghai
 *
 */
public class GetPropsHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		PropService propService = getService(PropService.class);
		GetPropsRs resp = propService.getProps(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(GetPropsRs.ext, resp);
		}
	}

}
