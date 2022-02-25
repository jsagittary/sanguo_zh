package com.gryphpoem.game.zw.handler.client.medal;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.DonateMedalRq;
import com.gryphpoem.game.zw.pb.GamePb1.DonateMedalRs;
import com.gryphpoem.game.zw.service.MedalService;
/**
 * 
* @ClassName: DonateMedalHandler
* @Description: 勋章捐献
* @author chenqi
* @date 2018年9月13日
*
 */
public class DonateMedalHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		DonateMedalRq req = msg.getExtension(DonateMedalRq.ext);
		MedalService medalService = getService(MedalService.class);
		DonateMedalRs resp = medalService.donateMedal(getRoleId(),req.getKeyIdList());

		if (null != resp) {
			sendMsgToPlayer(DonateMedalRs.ext, resp);
		}
	}

}
