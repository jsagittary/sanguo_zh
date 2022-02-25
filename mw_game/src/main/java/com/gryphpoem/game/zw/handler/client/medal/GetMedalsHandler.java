package com.gryphpoem.game.zw.handler.client.medal;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetMedalsRs;
import com.gryphpoem.game.zw.service.MedalService;
/**
 * 
* @ClassName: GetMedalsHandler
* @Description: 获取玩家所有勋章
* @author chenqi
* @date 2018年9月12日
*
 */
public class GetMedalsHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		MedalService medalService = getService(MedalService.class);
		GetMedalsRs resp = medalService.getMedals(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(GetMedalsRs.ext, resp);
		}
	}
}
