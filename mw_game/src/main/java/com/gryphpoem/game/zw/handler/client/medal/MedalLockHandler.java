package com.gryphpoem.game.zw.handler.client.medal;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.MedalLockRq;
import com.gryphpoem.game.zw.pb.GamePb1.MedalLockRs;
import com.gryphpoem.game.zw.service.MedalService;
/**
 * 
* @ClassName: MedalLockHandler
* @Description: 给勋章 上锁 或  解锁
* @author chenqi
* @date 2018年9月12日
*
 */
public class MedalLockHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		MedalLockRq req = msg.getExtension(MedalLockRq.ext);
		MedalService medalService = getService(MedalService.class);
		MedalLockRs resp = medalService.medalLock(getRoleId(),req.getKeyId());

		if (null != resp) {
			sendMsgToPlayer(MedalLockRs.ext, resp);
		}
	}

}
