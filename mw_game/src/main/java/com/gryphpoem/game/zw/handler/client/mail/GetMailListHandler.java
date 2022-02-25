package com.gryphpoem.game.zw.handler.client.mail;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetMailListRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetMailListRs;
import com.gryphpoem.game.zw.service.MailService;

/**
 * 
 * @Description 获取邮件列表
 * @author TanDonghai
 *
 */
public class GetMailListHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetMailListRq req = msg.getExtension(GetMailListRq.ext);
		MailService mailService = getService(MailService.class);
		GetMailListRs resp = mailService.getMailList(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(GetMailListRs.ext, resp);
		}
	}

}
