package com.gryphpoem.game.zw.handler.client.mail;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetMailByIdRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetMailByIdRs;
import com.gryphpoem.game.zw.service.MailService;

/**
 * 
 * @Description 根据邮件id获取邮件内容
 * @author TanDonghai
 *
 */
public class GetMailByIdHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetMailByIdRq req = msg.getExtension(GetMailByIdRq.ext);
		MailService mailService = getService(MailService.class);
		GetMailByIdRs resp = mailService.getMailById(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(GetMailByIdRs.ext, resp);
		}
	}

}
