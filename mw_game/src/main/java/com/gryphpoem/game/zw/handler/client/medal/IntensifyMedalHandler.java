package com.gryphpoem.game.zw.handler.client.medal;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.IntensifyMedalRq;
import com.gryphpoem.game.zw.pb.GamePb1.IntensifyMedalRs;
import com.gryphpoem.game.zw.service.MedalService;
/**
 * 
* @ClassName: IntensifyMedalHandler
* @Description: 勋章强化
* @author chenqi
* @date 2018年9月13日
*
 */
public class IntensifyMedalHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		IntensifyMedalRq req = msg.getExtension(IntensifyMedalRq.ext);
		MedalService medalService = getService(MedalService.class);
		IntensifyMedalRs resp = medalService.intensifyMedal(getRoleId(),req.getKeyId());

		if (null != resp) {
			sendMsgToPlayer(IntensifyMedalRs.ext, resp);
		}
	}

}
