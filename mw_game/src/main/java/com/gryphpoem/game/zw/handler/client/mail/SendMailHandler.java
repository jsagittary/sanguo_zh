package com.gryphpoem.game.zw.handler.client.mail;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.SendMailRq;
import com.gryphpoem.game.zw.pb.GamePb2.SendMailRs;
import com.gryphpoem.game.zw.service.MailService;

/**
 * 
 * @Description 发送邮件
 * @author TanDonghai
 *
 */
public class SendMailHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		SendMailRq req = msg.getExtension(SendMailRq.ext);
		MailService mailService = getService(MailService.class);
		SendMailRs resp = mailService.sendMail(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(SendMailRs.ext, resp);
		}
	}

}
