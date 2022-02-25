package com.gryphpoem.game.zw.handler.client.mail;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.RewardMailRq;
import com.gryphpoem.game.zw.pb.GamePb2.RewardMailRs;
import com.gryphpoem.game.zw.service.MailService;

/**
 * 
 * @Description 领取邮件奖励
 * @author TanDonghai
 *
 */
public class RewardMailHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		RewardMailRq req = msg.getExtension(RewardMailRq.ext);
		MailService mailService = getService(MailService.class);
		RewardMailRs resp = mailService.rewardMail(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(RewardMailRs.ext, resp);
		}
	}

}
