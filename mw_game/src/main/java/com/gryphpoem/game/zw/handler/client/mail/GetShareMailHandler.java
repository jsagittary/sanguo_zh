package com.gryphpoem.game.zw.handler.client.mail;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetShareMailRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetShareMailRs;
import com.gryphpoem.game.zw.service.MailService;

/**
 * 
 * @Description 获取玩家分享的邮件信息
 * @author TanDonghai
 *
 */
public class GetShareMailHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetShareMailRq req = msg.getExtension(GetShareMailRq.ext);
		MailService mailService = getService(MailService.class);
		GetShareMailRs resp = mailService.getShareMail(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(GetShareMailRs.ext, resp);
		}
	}

}
